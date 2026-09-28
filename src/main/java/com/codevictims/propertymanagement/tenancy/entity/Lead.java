package com.codevictims.propertymanagement.tenancy.entity;

import com.codevictims.propertymanagement.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "leasing_lead")
public class Lead extends BaseEntity {
  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = false)
  public Long unitId;

  @Column(nullable = false)
  public String name = "";

  @Column(nullable = false)
  public String phone = "";

  @Column(nullable = false, length = 40)
  public String status = "";

  @Column(nullable = true)
  public Instant viewingAt;

  @Column(nullable = true)
  public LocalDate followUpDate;

  @Column(nullable = false, columnDefinition = "TEXT")
  public String notes = "";
}
