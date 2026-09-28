package com.codevictims.propertymanagement.property.entity;

import com.codevictims.propertymanagement.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "meter")
public class Meter extends BaseEntity {
  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = true)
  public Long unitId;

  @Column(nullable = false)
  public String accountNumber = "";

  @Column(nullable = false, length = 40)
  public String kind = "";

  @Column(nullable = false, length = 40)
  public String responsibility = "";

  @Column(nullable = false)
  public boolean commonArea;

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal alertThreshold = BigDecimal.ZERO;
}
