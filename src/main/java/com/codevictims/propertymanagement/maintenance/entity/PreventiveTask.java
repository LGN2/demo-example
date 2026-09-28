package com.codevictims.propertymanagement.maintenance.entity;

import com.codevictims.propertymanagement.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "preventive_task")
public class PreventiveTask extends BaseEntity {
  @Transient public boolean seasonalPriority;

  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = true)
  public Long unitId;

  @Column(nullable = false)
  public String title = "";

  @Column(nullable = false, length = 40)
  public String category = "";

  @Column(nullable = false)
  public int intervalDays;

  @Column(nullable = false)
  public LocalDate nextDue;

  @Column(nullable = true)
  public LocalDate lastCompleted;

  @Column(nullable = false)
  public boolean enabled;
}
