package com.codevictims.propertymanagement.property.service;

import com.codevictims.propertymanagement.account.entity.UserAccount;
import com.codevictims.propertymanagement.billing.entity.Allocation;
import com.codevictims.propertymanagement.billing.entity.Due;
import com.codevictims.propertymanagement.billing.entity.Payment;
import com.codevictims.propertymanagement.billing.entity.TaxPolicy;
import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.entity.AuditEvent;
import com.codevictims.propertymanagement.common.exception.ApiException;
import com.codevictims.propertymanagement.common.repository.Store;
import com.codevictims.propertymanagement.common.service.Audit;
import com.codevictims.propertymanagement.maintenance.entity.Maintenance;
import com.codevictims.propertymanagement.property.entity.Building;
import com.codevictims.propertymanagement.property.entity.Unit;
import com.codevictims.propertymanagement.property.repository.UnitRepository;
import com.codevictims.propertymanagement.security.service.Access;
import com.codevictims.propertymanagement.tenancy.entity.Lease;
import com.codevictims.propertymanagement.tenancy.entity.Tenant;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class PropertyService {
  private final Store db;
  private final Access access;
  private final Audit audit;
  private final UnitRepository units;
  private final Clock clock;

  public PropertyService(Store db, Access access, Audit audit, UnitRepository units, Clock clock) {
    this.db = db;
    this.access = access;
    this.audit = audit;
    this.units = units;
    this.clock = clock;
  }

  public List<Building> buildings() {
    var ids = access.buildings();
    return ids.isEmpty()
        ? List.of()
        : db.list(
            Building.class,
            "select b from Building b where b.id in :ids order by b.id",
            "ids",
            ids);
  }

  public Building building(Input in, Long id) {
    Building b;
    if (id == null) {
      access.role("OWNER");
      b = new Building();
      b.ownerId = access.user().id;
    } else b = access.owner(id);
    b.name = in.text("name", 255);
    b.wilayat = in.text("wilayat", 255);
    b.address = in.text("address", 255);
    b.investmentValue = in.has("investmentValue") ? in.money("investmentValue", true) : null;
    b.reminderDays = in.integer("reminderDays", 1, 365, 90);
    b.seasonalStart = in.integer("seasonalStart", 0, 12, 5);
    b.seasonalEnd = in.integer("seasonalEnd", 0, 12, 9);
    db.save(b);
    audit.add(b.id, "BUILDING", b.id, id == null ? "CREATED" : "UPDATED", "");
    return b;
  }

  public List<Unit> units(Long building) {
    var ids = scope(building);
    if (ids.isEmpty()) return List.of();
    if (access.user().role.equals("TENANT"))
      return db.list(
          Unit.class,
          "select distinct u from Unit u, Lease l, Tenant t where u.id=l.unitId and l.tenantId=t.id"
              + " and t.accountId=:user and u.buildingId in :ids order by u.id",
          "user",
          access.user().id,
          "ids",
          ids);
    if (access.user().role.equals("VENDOR"))
      return db.list(
          Unit.class,
          "select distinct u from Unit u, Maintenance m where m.unitId=u.id and m.assignedTo=:user"
              + " and u.buildingId in :ids order by u.id",
          "user",
          access.user().id,
          "ids",
          ids);
    return db.list(
        Unit.class, "select u from Unit u where u.buildingId in :ids order by u.id", "ids", ids);
  }

  public List<Long> scope(Long building) {
    var ids = access.buildings();
    if (building != null) {
      access.building(building);
      return List.of(building);
    }
    return ids;
  }

  public Unit unit(Input in, Long id) {
    Long bid = in.id("buildingId");
    access.manage(bid);
    Unit u = id == null ? new Unit() : db.lock(Unit.class, id);
    if (id != null && !u.buildingId.equals(bid)) throw ApiException.invalid("INVALID_INPUT");
    u.buildingId = bid;
    u.code = in.text("code", 60);
    u.floorName = in.text("floorName", 80);
    u.size = in.money("size", true);
    u.kind = in.choice("kind", "RESIDENTIAL", "COMMERCIAL");
    u.availability = in.choice("availability", "AVAILABLE", "MAINTENANCE");
    u.marketRent = in.money("marketRent", false);
    if (id == null) u.vacancySince = LocalDate.now(clock);
    u.listing = in.has("listing") ? in.text("listing", 5000) : "";
    db.save(u);
    audit.add(bid, "UNIT", u.id, id == null ? "CREATED" : "UPDATED", "");
    return u;
  }

}
