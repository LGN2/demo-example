package com.codevictims.propertymanagement.security.service;

import com.codevictims.propertymanagement.account.entity.UserAccount;
import com.codevictims.propertymanagement.account.repository.BuildingAccessRepository;
import com.codevictims.propertymanagement.account.repository.UserRepository;
import com.codevictims.propertymanagement.common.exception.ApiException;
import com.codevictims.propertymanagement.common.repository.PersistenceSupport;
import com.codevictims.propertymanagement.maintenance.entity.Maintenance;
import com.codevictims.propertymanagement.maintenance.repository.MaintenanceRepository;
import com.codevictims.propertymanagement.maintenance.repository.VendorProfileRepository;
import com.codevictims.propertymanagement.property.entity.Building;
import com.codevictims.propertymanagement.property.entity.Unit;
import com.codevictims.propertymanagement.property.repository.BuildingRepository;
import com.codevictims.propertymanagement.tenancy.entity.Lease;
import com.codevictims.propertymanagement.tenancy.entity.Tenant;
import com.codevictims.propertymanagement.tenancy.repository.LeaseRepository;
import com.codevictims.propertymanagement.tenancy.repository.TenantRepository;
import java.util.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class Access {
  private final BuildingRepository buildingRepository;
  private final BuildingAccessRepository buildingAccessRepository;
  private final TenantRepository tenantRepository;
  private final VendorProfileRepository vendorProfileRepository;
  private final LeaseRepository leaseRepository;
  private final MaintenanceRepository maintenanceRepository;
  private final UserRepository users;
  private final PersistenceSupport db;

  public Access(
      UserRepository users,
      PersistenceSupport db,
      BuildingRepository buildingRepository,
      BuildingAccessRepository buildingAccessRepository,
      TenantRepository tenantRepository,
      VendorProfileRepository vendorProfileRepository,
      LeaseRepository leaseRepository,
      MaintenanceRepository maintenanceRepository) {
    this.buildingRepository = buildingRepository;
    this.buildingAccessRepository = buildingAccessRepository;
    this.tenantRepository = tenantRepository;
    this.vendorProfileRepository = vendorProfileRepository;
    this.leaseRepository = leaseRepository;
    this.maintenanceRepository = maintenanceRepository;
    this.users = users;
    this.db = db;
  }

  public UserAccount user() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) throw new ApiException(401, "UNAUTHENTICATED");
    var u =
        users
            .findByUsername(auth.getName())
            .orElseThrow(() -> new ApiException(401, "UNAUTHENTICATED"));
    if (!u.active) throw new ApiException(401, "UNAUTHENTICATED");
    return u;
  }

  public void role(String... allowed) {
    if (!Arrays.asList(allowed).contains(user().role)) throw ApiException.forbidden();
  }

  public List<Long> buildings() {
    UserAccount u = user();
    return switch (u.role) {
      case "OWNER" -> buildingRepository.findIdsByOwner(u.id);
      case "MANAGER", "GUARD" -> buildingAccessRepository.findBuildingIdsForUser(u.id);
      case "TENANT" -> tenantRepository.findBuildingIdsForAccount(u.id);
      case "VENDOR" -> vendorProfileRepository.findBuildingIdsForUser(u.id);
      default -> List.of();
    };
  }

  public Building building(Long id) {
    if (!buildings().contains(id)) throw ApiException.missing();
    return db.get(Building.class, id);
  }

  public Building manage(Long id) {
    role("OWNER", "MANAGER");
    Building b = building(id);
    if (user().role.equals("MANAGER")
        && buildingAccessRepository.findWritableAssignment(user().id, id).isEmpty())
      throw ApiException.forbidden();
    return b;
  }

  public Building owner(Long id) {
    role("OWNER");
    return building(id);
  }

  public void staffRead(Long id) {
    role("OWNER", "MANAGER");
    building(id);
  }

  public Tenant tenant(Long id) {
    Tenant t = db.get(Tenant.class, id);
    if (user().role.equals("TENANT")) {
      if (!Objects.equals(t.accountId, user().id)) throw ApiException.missing();
    } else staffRead(t.buildingId);
    return t;
  }

  public Lease lease(Long id, boolean write) {
    Lease l = db.get(Lease.class, id);
    if (write) manage(l.buildingId);
    else if (user().role.equals("TENANT")) tenant(l.tenantId);
    else staffRead(l.buildingId);
    return l;
  }

  public Unit unit(Long id) {
    Unit u = db.get(Unit.class, id);
    if (user().role.equals("TENANT")) {
      if (leaseRepository.findIdsForTenantAccountAndUnit(id, user().id).isEmpty())
        throw ApiException.missing();
    } else if (user().role.equals("VENDOR")) {
      if (maintenanceRepository.findAssignedIdsForUnit(id, user().id).isEmpty())
        throw ApiException.missing();
    } else building(u.buildingId);
    return u;
  }

  public Maintenance maintenance(Long id) {
    Maintenance m = db.get(Maintenance.class, id);
    switch (user().role) {
      case "TENANT" -> {
        if (m.tenantId == null) throw ApiException.missing();
        tenant(m.tenantId);
      }
      case "VENDOR" -> {
        if (!Objects.equals(m.assignedTo, user().id)) throw ApiException.missing();
      }
      default -> staffRead(m.buildingId);
    }
    return m;
  }

  public UserAccount portfolioUser(Long id, Long buildingId, String... roles) {
    Building b = db.get(Building.class, buildingId);
    UserAccount target = db.get(UserAccount.class, id);
    if (!target.active
        || !Objects.equals(target.ownerId, b.ownerId)
        || !Arrays.asList(roles).contains(target.role))
      throw ApiException.invalid("INVALID_ASSIGNEE");
    return target;
  }
}
