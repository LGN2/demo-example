package com.codevictims.propertymanagement.maintenance.service;

import com.codevictims.propertymanagement.account.repository.BuildingAccessRepository;
import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.exception.ApiException;
import com.codevictims.propertymanagement.common.repository.PersistenceSupport;
import com.codevictims.propertymanagement.common.service.AuditService;
import com.codevictims.propertymanagement.maintenance.entity.Maintenance;
import com.codevictims.propertymanagement.maintenance.repository.MaintenanceRepository;
import com.codevictims.propertymanagement.maintenance.repository.VendorProfileRepository;
import com.codevictims.propertymanagement.property.entity.Building;
import com.codevictims.propertymanagement.property.entity.Unit;
import com.codevictims.propertymanagement.property.service.PropertyService;
import com.codevictims.propertymanagement.security.service.Access;
import com.codevictims.propertymanagement.tenancy.entity.Tenant;
import com.codevictims.propertymanagement.tenancy.repository.LeaseRepository;
import com.codevictims.propertymanagement.tenancy.repository.TenantRepository;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class MaintenanceService {
  private final MaintenanceRepository maintenanceRepository;
  private final TenantRepository tenantRepository;
  private final LeaseRepository leaseRepository;
  private final BuildingAccessRepository buildingAccessRepository;
  private final VendorProfileRepository vendorProfileRepository;
  public static final String[] CATEGORIES = {"AC", "PLUMBING", "ELECTRICAL", "LIFT", "OTHER"};
  private final PersistenceSupport db;
  private final Access access;
  private final AuditService audit;
  private final Clock clock;
  private final PropertyService properties;

  public MaintenanceService(
      PersistenceSupport db,
      Access access,
      AuditService audit,
      Clock clock,
      PropertyService properties,
      MaintenanceRepository maintenanceRepository,
      TenantRepository tenantRepository,
      LeaseRepository leaseRepository,
      BuildingAccessRepository buildingAccessRepository,
      VendorProfileRepository vendorProfileRepository) {
    this.maintenanceRepository = maintenanceRepository;
    this.tenantRepository = tenantRepository;
    this.leaseRepository = leaseRepository;
    this.buildingAccessRepository = buildingAccessRepository;
    this.vendorProfileRepository = vendorProfileRepository;
    this.db = db;
    this.access = access;
    this.audit = audit;
    this.clock = clock;
    this.properties = properties;
  }

  public List<Maintenance> list(Long building) {
    var rows = listScoped(building);
    int month = LocalDate.now(clock).getMonthValue();
    for (var m : rows) {
      Building b = db.get(Building.class, m.buildingId);
      m.seasonalPriority =
          m.category.equals("AC") && inSeason(month, b.seasonalStart, b.seasonalEnd);
    }
    return rows;
  }

  public static boolean inSeason(int month, int start, int end) {
    return start > 0
        && end > 0
        && (start <= end ? month >= start && month <= end : month >= start || month <= end);
  }

  private List<Maintenance> listScoped(Long building) {
    access.role("OWNER", "MANAGER", "TENANT", "VENDOR");
    var ids = properties.scope(building);
    if (ids.isEmpty()) return List.of();
    if (access.user().role.equals("TENANT"))
      return maintenanceRepository.findForTenantAccountInBuildings(access.user().id, ids);
    if (access.user().role.equals("VENDOR"))
      return maintenanceRepository.findAssignedInBuildings(access.user().id, ids);
    return maintenanceRepository.findInBuildings(ids);
  }

  public Maintenance create(Input in) {
    access.role("OWNER", "MANAGER", "TENANT");
    Unit u = access.unit(in.id("unitId"));
    Maintenance m = new Maintenance();
    m.unitId = u.id;
    m.buildingId = u.buildingId;
    if (access.user().role.equals("TENANT")) {
      var candidates =
          tenantRepository.findCurrentTenantForUnit(u.id, access.user().id, LocalDate.now(clock));
      if (candidates.isEmpty()) throw ApiException.forbidden();
      m.tenantId = candidates.get(0).id;
    } else {
      access.manage(u.buildingId);
      m.tenantId = in.nullableId("tenantId");
      if (m.tenantId != null) {
        Tenant t = db.get(Tenant.class, m.tenantId);
        if (!t.buildingId.equals(u.buildingId)
            || leaseRepository.findIdsByTenantAndUnit(t.id, u.id).isEmpty())
          throw ApiException.invalid("INVALID_TENANT");
      }
    }
    m.description = in.text("description", 2000);
    m.category = in.choice("category", CATEGORIES);
    m.urgent = in.bool("urgent");
    m.status = "OPEN";
    m.aiStatus = "NOT_REQUESTED";
    db.save(m);
    audit.add(m.buildingId, "MAINTENANCE", m.id, "OPENED", m.urgent ? "URGENT" : "NORMAL");
    return m;
  }

  public Maintenance transition(Long id, Input in) {
    Maintenance initial = access.maintenance(id);
    Maintenance m = db.lock(Maintenance.class, initial.id);
    String next = in.choice("status", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED");
    if (access.user().role.equals("VENDOR")) {
      if (!Set.of("IN_PROGRESS", "RESOLVED").contains(next)) throw ApiException.forbidden();
    } else access.manage(m.buildingId);
    String expected =
        switch (m.status) {
          case "OPEN" -> "ASSIGNED";
          case "ASSIGNED" -> "IN_PROGRESS";
          case "IN_PROGRESS" -> "RESOLVED";
          case "RESOLVED" -> "CLOSED";
          default -> "";
        };
    if (!next.equals(expected)) throw ApiException.conflict("INVALID_TRANSITION");
    if (next.equals("ASSIGNED")) {
      Long assignee = in.id("assignedTo");
      var target = access.portfolioUser(assignee, m.buildingId, "MANAGER", "VENDOR", "OWNER");
      boolean permitted =
          target.role.equals("OWNER")
              && target.id.equals(db.get(Building.class, m.buildingId).ownerId);
      permitted |=
          target.role.equals("MANAGER")
              && !buildingAccessRepository
                  .findWritableByUserAndBuilding(assignee, m.buildingId)
                  .isEmpty();
      permitted |=
          target.role.equals("VENDOR")
              && !vendorProfileRepository
                  .findForMaintenanceTransition(assignee, m.buildingId)
                  .isEmpty();
      if (!permitted) throw ApiException.invalid("INVALID_ASSIGNEE");
      m.assignedTo = assignee;
    }
    m.status = next;
    if (next.equals("RESOLVED")) m.resolvedAt = clock.instant();
    if (next.equals("CLOSED")) m.closedAt = clock.instant();
    audit.add(m.buildingId, "MAINTENANCE", id, next, in.optional("note"));
    return m;
  }

  public Maintenance approve(Long id, Input in) {
    Maintenance m = access.maintenance(id);
    access.manage(m.buildingId);
    m.approvedSummary = in.text("summary", 255);
    m.category = in.choice("category", CATEGORIES);
    audit.add(m.buildingId, "MAINTENANCE", id, "SUMMARY_APPROVED", m.category);
    return m;
  }

  public void comment(Long id, Input in) {
    Maintenance m = access.maintenance(id);
    audit.add(m.buildingId, "MAINTENANCE", id, "COMMENT", in.text("note", 2000));
  }

  public Maintenance aiResult(Long id, AiAssistant.Result result) {
    Maintenance m = access.maintenance(id);
    access.manage(m.buildingId);
    m.aiStatus = result.status();
    if (result.suggestion() != null) {
      m.proposedSummary = result.suggestion().summary();
      m.proposedCategory = result.suggestion().category();
    }
    audit.add(m.buildingId, "MAINTENANCE", id, "AI_" + result.status(), "");
    return m;
  }
}
