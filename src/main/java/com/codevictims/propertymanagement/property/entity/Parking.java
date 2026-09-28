package com.codevictims.propertymanagement.property.entity;

import com.codevictims.propertymanagement.common.entity.Row;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "parking")
public class Parking extends Row {
  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = false)
  public Long unitId;

  @Column(nullable = false)
  public String space = "";

  @Column(nullable = false)
  public String vehicle = "";
}
