package om.bayt.service;

import java.time.*;
import java.util.*;
import om.bayt.api.*;
import om.bayt.domain.*;
import om.bayt.security.Access;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MaintenanceService {
  public static final String[] CATEGORIES = {"AC", "PLUMBING", "ELECTRICAL", "LIFT", "OTHER"};
  private final Store db;
  private final Access access;
  private final Audit audit;
  private final Clock clock;
  private final PropertyService properties;

  public MaintenanceService(
      Store db, Access access, Audit audit, Clock clock, PropertyService properties) {
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
      return db.list(
          Maintenance.class,
          "select m from Maintenance m,Tenant t where m.tenantId=t.id and t.accountId=:u and"
              + " m.buildingId in :ids order by m.id desc",
          "u",
          access.user().id,
          "ids",
          ids);
    if (access.user().role.equals("VENDOR"))
      return db.list(
          Maintenance.class,
          "select m from Maintenance m where m.assignedTo=:u and m.buildingId in :ids order by m.id"
              + " desc",
          "u",
          access.user().id,
          "ids",
          ids);
    return db.list(
        Maintenance.class,
        "select m from Maintenance m where m.buildingId in :ids order by m.id desc",
        "ids",
        ids);
  }

  public Maintenance create(Input in) {
    access.role("OWNER", "MANAGER", "TENANT");
    Unit u = access.unit(in.id("unitId"));
    Maintenance m = new Maintenance();
    m.unitId = u.id;
    m.buildingId = u.buildingId;
    if (access.user().role.equals("TENANT")) {
      var candidates =
          db.list(
              Tenant.class,
              "select distinct t from Tenant t,Lease l where l.tenantId=t.id and l.unitId=:unit and"
                  + " t.accountId=:user and l.startDate<=:today and l.endDate>=:today and"
                  + " (l.terminatedOn is null or l.terminatedOn>=:today)",
              "unit",
              u.id,
              "user",
              access.user().id,
              "today",
              LocalDate.now(clock));
      if (candidates.isEmpty()) throw ApiException.forbidden();
      m.tenantId = candidates.get(0).id;
    } else {
      access.manage(u.buildingId);
      m.tenantId = in.nullableId("tenantId");
      if (m.tenantId != null) {
        Tenant t = db.get(Tenant.class, m.tenantId);
        if (!t.buildingId.equals(u.buildingId)
            || db.list(
                    Long.class,
                    "select l.id from Lease l where l.tenantId=:t and l.unitId=:u",
                    "t",
                    t.id,
                    "u",
                    u.id)
                .isEmpty()) throw ApiException.invalid("INVALID_TENANT");
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
              && !db.list(
                      BuildingAccess.class,
                      "select a from BuildingAccess a where a.userId=:u and a.buildingId=:b and"
                          + " a.canWrite=true",
                      "u",
                      assignee,
                      "b",
                      m.buildingId)
                  .isEmpty();
      permitted |=
          target.role.equals("VENDOR")
              && !db.list(
                      VendorProfile.class,
                      "select v from VendorProfile v where v.userId=:u and v.buildingId=:b",
                      "u",
                      assignee,
                      "b",
                      m.buildingId)
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
