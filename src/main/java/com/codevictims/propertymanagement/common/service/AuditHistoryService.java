package com.codevictims.propertymanagement.common.service;

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

import com.codevictims.propertymanagement.property.service.PropertyService;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class AuditHistoryService {
  private final Store db;
  private final Access access;
  private final Audit audit;
  private final UnitRepository units;
  private final Clock clock;
  private final PropertyService properties;

  public AuditHistoryService(Store db, Access access, Audit audit, UnitRepository units, Clock clock, PropertyService properties) {
    this.db = db;
    this.access = access;
    this.audit = audit;
    this.units = units;
    this.clock = clock;
    this.properties = properties;
  }

  public List<AuditEvent> history(String type, Long id) {
    Long bid =
        switch (type) {
          case "LEASE" -> access.lease(id, false).buildingId;
          case "MAINTENANCE" -> access.maintenance(id).buildingId;
          case "BUILDING" -> access.building(id).id;
          default -> throw ApiException.invalid("INVALID_INPUT");
        };
    if (type.equals("BUILDING")) access.staffRead(bid);
    var events = db.list(
        AuditEvent.class,
        "select a from AuditEvent a where a.resourceType=:type and a.resourceId=:id order by a.createdAt",
        "type", type, "id", id);
    if (access.user().role.equals("TENANT"))
      return events.stream().filter(event -> !event.action.equals("FOLLOW_UP")).toList();
    return events;
  }}
