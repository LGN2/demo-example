package om.bayt.security;

import java.util.*;
import om.bayt.api.ApiException;
import om.bayt.domain.*;
import om.bayt.service.Store;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class Access {
  private final UserRepository users;
  private final Store db;

  public Access(UserRepository users, Store db) {
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
      case "OWNER" ->
          db.list(Long.class, "select b.id from Building b where b.ownerId=:id", "id", u.id);
      case "MANAGER", "GUARD" ->
          db.list(
              Long.class,
              "select a.buildingId from BuildingAccess a where a.userId=:id",
              "id",
              u.id);
      case "TENANT" ->
          db.list(
              Long.class,
              "select distinct t.buildingId from Tenant t where t.accountId=:id",
              "id",
              u.id);
      case "VENDOR" ->
          db.list(
              Long.class,
              "select v.buildingId from VendorProfile v where v.userId=:id",
              "id",
              u.id);
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
        && db.list(
                BuildingAccess.class,
                "select a from BuildingAccess a where a.userId=:user and a.buildingId=:building and"
                    + " a.canWrite=true",
                "user",
                user().id,
                "building",
                id)
            .isEmpty()) throw ApiException.forbidden();
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
      if (db.list(
              Long.class,
              "select l.id from Lease l, Tenant t where l.tenantId=t.id and l.unitId=:unit and"
                  + " t.accountId=:user",
              "unit",
              id,
              "user",
              user().id)
          .isEmpty()) throw ApiException.missing();
    } else if (user().role.equals("VENDOR")) {
      if (db.list(
              Long.class,
              "select m.id from Maintenance m where m.unitId=:unit and m.assignedTo=:user",
              "unit",
              id,
              "user",
              user().id)
          .isEmpty()) throw ApiException.missing();
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
