package com.codevictims.propertymanagement.property.entity;

import com.codevictims.propertymanagement.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "unit")
public class Unit extends BaseEntity {
  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = false)
  public String code = "";

  @Column(nullable = false)
  public String floorName = "";

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal size = BigDecimal.ZERO;

  @Column(nullable = false, length = 40)
  public String kind = "";

  @Column(nullable = false, length = 40)
  public String availability = "";

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal marketRent = BigDecimal.ZERO;

  @Column(nullable = false)
  public LocalDate vacancySince;

  @Column(nullable = false, columnDefinition = "TEXT")
  public String listing = "";
}
