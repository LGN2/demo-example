package com.codevictims.propertymanagement.billing.entity;

import com.codevictims.propertymanagement.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "follow_up")
public class FollowUp extends BaseEntity {
  @Column(nullable = false)
  public Long leaseId;

  @Column(nullable = false)
  public String note = "";

  @Column(nullable = true)
  public LocalDate nextDate;
}
