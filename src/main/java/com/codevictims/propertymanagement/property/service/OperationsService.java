package com.codevictims.propertymanagement.property.service;

import com.codevictims.propertymanagement.account.entity.BuildingAccess;
import com.codevictims.propertymanagement.account.entity.UserAccount;
import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.entity.Row;
import com.codevictims.propertymanagement.common.exception.ApiException;
import com.codevictims.propertymanagement.common.repository.Store;
import com.codevictims.propertymanagement.common.service.Audit;
import com.codevictims.propertymanagement.maintenance.entity.Maintenance;
import com.codevictims.propertymanagement.maintenance.entity.PreventiveTask;
import com.codevictims.propertymanagement.maintenance.entity.VendorProfile;
import com.codevictims.propertymanagement.maintenance.service.MaintenanceService;
import com.codevictims.propertymanagement.property.entity.Building;
import com.codevictims.propertymanagement.property.entity.GuardCheckIn;
import com.codevictims.propertymanagement.property.entity.Meter;
import com.codevictims.propertymanagement.property.entity.MeterReading;
import com.codevictims.propertymanagement.property.entity.Notice;
import com.codevictims.propertymanagement.property.entity.Parking;
import com.codevictims.propertymanagement.property.entity.SafetyRecord;
import com.codevictims.propertymanagement.property.entity.Unit;
import com.codevictims.propertymanagement.property.entity.Visit;
import com.codevictims.propertymanagement.security.service.Access;
import com.codevictims.propertymanagement.tenancy.entity.Lead;

import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class OperationsService {
  private final Store db;
  private final Access access;
  private final Audit audit;
  private final PropertyService properties;
  private final Clock clock;
  private static final Map<String, Class<? extends Row>> TYPES =
      Map.of(
          "safety",
          SafetyRecord.class,
          "preventive",
          PreventiveTask.class,
          "vendors",
          VendorProfile.class,
          "meters",
          Meter.class,
          "notices",
          Notice.class,
          "leads",
          Lead.class,
          "visits",
          Visit.class,
          "checkins",
          GuardCheckIn.class,
          "parking",
          Parking.class);

  public OperationsService(
      Store db, Access access, Audit audit, PropertyService properties, Clock clock) {
    this.db = db;
    this.access = access;
    this.audit = audit;
    this.properties = properties;
    this.clock = clock;
  }

  public List<? extends Row> list(String type, Long building) {
    Class<? extends Row> entity = type(type);
    readRole(type);
    var ids = properties.scope(building);
    if (ids.isEmpty()) return List.of();
    if (access.user().role.equals("TENANT") && !type.equals("notices")) {
      var unitIds = properties.units(building).stream().map(u -> u.id).toList();
      if (unitIds.isEmpty()) return List.of();
      return db.list(
          entity,
          "select e from "
              + entity.getSimpleName()
              + " e where e.buildingId in :ids and e.unitId in :units order by e.id desc",
          "ids",
          ids,
          "units",
          unitIds);
    }
    var rows =
        db.list(
            entity,
            "select e from "
                + entity.getSimpleName()
                + " e where e.buildingId in :ids order by e.id desc",
            "ids",
            ids);
    if (type.equals("preventive"))
      for (Row row : rows) {
        PreventiveTask p = (PreventiveTask) row;
        Building b = db.get(Building.class, p.buildingId);
        p.seasonalPriority =
            p.category.equals("AC")
                && MaintenanceService.inSeason(
                    LocalDate.now(clock).getMonthValue(), b.seasonalStart, b.seasonalEnd);
      }
    return rows;
  }

  public Row get(String type, Long id) {
    return list(type, null).stream()
        .filter(r -> r.id.equals(id))
        .findFirst()
        .orElseThrow(ApiException::missing);
  }

  private Class<? extends Row> type(String type) {
    var t = TYPES.get(type);
    if (t == null) throw ApiException.missing();
    return t;
  }

  private void readRole(String type) {
    if (type.equals("notices")) access.role("OWNER", "MANAGER", "TENANT");
    else if (type.equals("meters")) access.role("OWNER", "MANAGER", "TENANT");
    else if (type.equals("parking")) access.role("OWNER", "MANAGER", "TENANT", "GUARD");
    else if (type.equals("visits") || type.equals("checkins"))
      access.role("OWNER", "MANAGER", "GUARD");
    else access.role("OWNER", "MANAGER");
  }

  public Row save(String type, Long id, Input in) {
    type(type);
    Long b = in.id("buildingId");
    if (access.user().role.equals("GUARD") && Set.of("visits", "checkins").contains(type)) {
      access.building(b);
      if (id != null) throw ApiException.forbidden();
    } else access.manage(b);
    Row row = id == null ? null : get(type, id);
    if (row != null && !Objects.equals(buildingOf(row), b))
      throw ApiException.invalid("INVALID_INPUT");
    Row saved =
        switch (type) {
          case "safety" -> {
            SafetyRecord x = row == null ? new SafetyRecord() : (SafetyRecord) row;
            x.buildingId = b;
            x.kind =
                in.choice(
                    "kind",
                    "FIRE_EXTINGUISHER",
                    "FIRE_ALARM",
                    "CIVIL_DEFENCE",
                    "LIFT_INSPECTION",
                    "SERVICE_CONTRACT",
                    "INSURANCE",
                    "PERMIT",
                    "COMPLETION",
                    "OTHER");
            x.reference = in.text("reference", 255);
            x.expiryDate = in.date("expiryDate");
            x.notes = in.optional("notes");
            yield db.save(x);
          }
          case "preventive" -> {
            PreventiveTask x = row == null ? new PreventiveTask() : (PreventiveTask) row;
            x.buildingId = b;
            x.unitId = unit(in, b, true);
            x.title = in.text("title", 255);
            x.category =
                in.choice(
                    "category",
                    "AC",
                    "PLUMBING",
                    "ELECTRICAL",
                    "LIFT",
                    "WATER_TANK",
                    "PEST_CONTROL",
                    "GENERATOR",
                    "OTHER");
            x.intervalDays = in.integer("intervalDays", 1, 3650, 90);
            x.nextDue = in.date("nextDue");
            x.enabled = in.bool("enabled");
            yield db.save(x);
          }
          case "vendors" -> {
            VendorProfile x = row == null ? new VendorProfile() : (VendorProfile) row;
            x.buildingId = b;
            x.userId = access.portfolioUser(in.id("userId"), b, "VENDOR").id;
            x.name = in.text("name", 255);
            x.categories = in.text("categories", 255);
            for (String c : x.categories.split(","))
              if (!Set.of("AC", "PLUMBING", "ELECTRICAL", "LIFT", "OTHER").contains(c.trim()))
                throw ApiException.invalid("INVALID_INPUT");
            x.hourlyRate = in.money("hourlyRate", false);
            yield db.save(x);
          }
          case "meters" -> {
            Meter x = row == null ? new Meter() : (Meter) row;
            x.buildingId = b;
            Long unit = unit(in, b, true);
            String kind = in.choice("kind", "WATER", "ELECTRICITY");
            if (row != null && (!Objects.equals(x.unitId, unit) || !x.kind.equals(kind)))
              throw ApiException.invalid("INVALID_INPUT");
            x.unitId = unit;
            x.accountNumber = in.text("accountNumber", 255);
            x.kind = kind;
            x.responsibility = in.choice("responsibility", "OWNER", "TENANT");
            x.commonArea = in.bool("commonArea");
            if (x.commonArea && x.unitId != null) throw ApiException.invalid("INVALID_INPUT");
            x.alertThreshold = in.money("alertThreshold", false);
            yield db.save(x);
          }
          case "notices" -> {
            Notice x = row == null ? new Notice() : (Notice) row;
            x.buildingId = b;
            x.titleAr = in.text("titleAr", 255);
            x.titleEn = in.text("titleEn", 255);
            x.bodyAr = in.text("bodyAr", 5000);
            x.bodyEn = in.text("bodyEn", 5000);
            yield db.save(x);
          }
          case "leads" -> {
            Lead x = row == null ? new Lead() : (Lead) row;
            x.buildingId = b;
            x.unitId = unit(in, b, false);
            x.name = in.text("name", 255);
            x.phone = in.text("phone", 80);
            x.status = in.choice("status", "NEW", "CONTACTED", "VIEWING", "WON", "LOST");
            x.viewingAt = in.has("viewingAt") ? in.instant("viewingAt") : null;
            x.followUpDate = in.optionalDate("followUpDate");
            x.notes = in.has("notes") ? in.text("notes", 2000) : "";
            yield db.save(x);
          }
          case "visits" -> {
            if (row != null) throw ApiException.forbidden();
            Visit x = new Visit();
            x.buildingId = b;
            x.unitId = unit(in, b, true);
            x.visitorName = in.text("visitorName", 255);
            x.purpose = in.text("purpose", 255);
            x.checkIn = clock.instant();
            yield db.save(x);
          }
          case "checkins" -> {
            if (row != null) throw ApiException.forbidden();
            GuardCheckIn x = new GuardCheckIn();
            x.buildingId = b;
            x.userId = access.user().id;
            x.note = in.optional("note");
            yield db.save(x);
          }
          case "parking" -> {
            Parking x = row == null ? new Parking() : (Parking) row;
            x.buildingId = b;
            x.unitId = unit(in, b, false);
            x.space = in.text("space", 50);
            x.vehicle = in.text("vehicle", 255);
            yield db.save(x);
          }
          default -> throw ApiException.missing();
        };
    audit.add(b, type.toUpperCase(Locale.ROOT), saved.id, id == null ? "CREATED" : "UPDATED", "");
    return saved;
  }

  private Long buildingOf(Row r) {
    try {
      return (Long) r.getClass().getField("buildingId").get(r);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  private Long unit(Input in, Long building, boolean optional) {
    Long id = optional ? in.nullableId("unitId") : in.id("unitId");
    if (id != null && !db.get(Unit.class, id).buildingId.equals(building))
      throw ApiException.invalid("INVALID_INPUT");
    return id;
  }

  public Visit checkout(Long id) {
    Visit v = (Visit) get("visits", id);
    if (access.user().role.equals("GUARD")) access.building(v.buildingId);
    else access.manage(v.buildingId);
    if (v.checkOut == null) {
      v.checkOut = clock.instant();
      audit.add(v.buildingId, "VISIT", id, "CHECKED_OUT", "");
    }
    return v;
  }

  public PreventiveTask complete(Long id) {
    PreventiveTask p = db.lock(PreventiveTask.class, id);
    access.manage(p.buildingId);
    LocalDate today = LocalDate.now(clock);
    if (!p.enabled || p.nextDue.isAfter(today)) throw ApiException.conflict("NOT_DUE");
    p.lastCompleted = today;
    do {
      p.nextDue = p.nextDue.plusDays(p.intervalDays);
    } while (!p.nextDue.isAfter(today));
    audit.add(p.buildingId, "PREVENTIVE", id, "COMPLETED", p.title);
    return p;
  }

  public List<MeterReading> readings(Long id) {
    get("meters", id);
    return db.list(
        MeterReading.class,
        "select r from MeterReading r where r.meterId=:id order by r.readingDate desc,r.id desc",
        "id",
        id);
  }

  public MeterReading reading(Long id, Input in) {
    Meter m = db.lock(Meter.class, id);
    access.manage(m.buildingId);
    MeterReading r = new MeterReading();
    r.meterId = id;
    r.readingDate = in.date("readingDate");
    r.value = in.money("value", false);
    r.kind = in.choice("kind", "ROUTINE", "MOVE_IN", "MOVE_OUT");
    if (r.readingDate.isAfter(LocalDate.now(clock))) throw ApiException.invalid("INVALID_DATE");
    var old = readings(id);
    r.observation = "NORMAL";
    if (!old.isEmpty()) {
      var last = old.get(0);
      if (!r.readingDate.isAfter(last.readingDate) || r.value.compareTo(last.value) < 0)
        throw ApiException.invalid("INVALID_READING");
      if (m.alertThreshold.signum() > 0
          && r.value.subtract(last.value).compareTo(m.alertThreshold) > 0)
        r.observation = "UNUSUAL_CONSUMPTION";
    }
    db.save(r);
    audit.add(m.buildingId, "METER", id, "READING_RECORDED", r.observation);
    return r;
  }

  public List<Map<String, Object>> vendorPerformance(Long building) {
    access.role("OWNER", "MANAGER");
    List<Map<String, Object>> result = new ArrayList<>();
    for (var row : list("vendors", building)) {
      VendorProfile v = (VendorProfile) row;
      var jobs =
          db.list(
              Maintenance.class,
              "select m from Maintenance m where m.assignedTo=:u and m.buildingId=:b",
              "u",
              v.userId,
              "b",
              v.buildingId);
      long complete = jobs.stream().filter(m -> m.resolvedAt != null).count();
      double hours =
          jobs.stream()
                  .filter(m -> m.resolvedAt != null)
                  .mapToLong(m -> Duration.between(m.createdAt, m.resolvedAt).toMinutes())
                  .average()
                  .orElse(0)
              / 60.0;
      result.add(
          Map.of(
              "vendor",
              v,
              "assignedJobs",
              jobs.size(),
              "completedJobs",
              complete,
              "meanResolutionHours",
              hours));
    }
    return result;
  }

  public List<UserAccount> assignees(Long building) {
    access.staffRead(building);
    Building b = db.get(Building.class, building);
    return db.list(
        UserAccount.class,
        "select distinct u from UserAccount u where u.active=true and (u.id=:owner or u.id in"
            + " (select a.userId from BuildingAccess a where a.buildingId=:b and a.canWrite=true)"
            + " or u.id in (select v.userId from VendorProfile v where v.buildingId=:b))",
        "owner",
        b.ownerId,
        "b",
        building);
  }
}
