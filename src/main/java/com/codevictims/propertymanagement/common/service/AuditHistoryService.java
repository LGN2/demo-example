package com.codevictims.propertymanagement.common.service;

import com.codevictims.propertymanagement.common.entity.AuditEvent;
import com.codevictims.propertymanagement.common.exception.ApiException;
import com.codevictims.propertymanagement.common.repository.AuditEventRepository;
import com.codevictims.propertymanagement.common.repository.PersistenceSupport;
import com.codevictims.propertymanagement.property.repository.UnitRepository;
import com.codevictims.propertymanagement.property.service.PropertyService;
import com.codevictims.propertymanagement.security.service.Access;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class AuditHistoryService {
  private final AuditEventRepository auditEventRepository;
  private final PersistenceSupport db;
  private final Access access;
  private final AuditService audit;
  private final UnitRepository units;
  private final Clock clock;
  private final PropertyService properties;

  public AuditHistoryService(
      PersistenceSupport db,
      Access access,
      AuditService audit,
      UnitRepository units,
      Clock clock,
      PropertyService properties,
      AuditEventRepository auditEventRepository) {
    this.auditEventRepository = auditEventRepository;
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
    var events = auditEventRepository.findHistoryForResource(type, id);
    if (access.user().role.equals("TENANT"))
      return events.stream().filter(event -> !event.action.equals("FOLLOW_UP")).toList();
    return events;
  }
}
