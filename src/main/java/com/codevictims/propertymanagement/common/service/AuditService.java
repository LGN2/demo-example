package com.codevictims.propertymanagement.common.service;

import com.codevictims.propertymanagement.common.entity.AuditEvent;
import com.codevictims.propertymanagement.common.repository.PersistenceSupport;
import com.codevictims.propertymanagement.security.service.Access;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
  private final PersistenceSupport db;
  private final Access access;

  public AuditService(PersistenceSupport db, Access access) {
    this.db = db;
    this.access = access;
  }

  public void add(Long building, String type, Long id, String action, String note) {
    AuditEvent e = new AuditEvent();
    e.buildingId = building;
    e.resourceType = type;
    e.resourceId = id;
    e.actorId = access.user().id;
    e.action = action;
    e.note = note;
    db.save(e);
  }
}
