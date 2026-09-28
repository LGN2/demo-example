package com.codevictims.propertymanagement.common.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "audit_event")
public class AuditEvent extends BaseEntity {
  @Column(nullable = true)
  public Long buildingId;

  @Column(nullable = false, length = 40)
  public String resourceType = "";

  @Column(nullable = false)
  public Long resourceId;

  @Column(nullable = false)
  public Long actorId;

  @Column(nullable = false, length = 40)
  public String action = "";

  @Column(nullable = false, columnDefinition = "TEXT")
  public String note = "";
}
