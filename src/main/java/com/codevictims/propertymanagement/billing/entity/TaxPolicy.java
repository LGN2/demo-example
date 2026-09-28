package com.codevictims.propertymanagement.billing.entity;

import com.codevictims.propertymanagement.common.entity.Row;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "tax_policy")
public class TaxPolicy extends Row {
  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = false, length = 40)
  public String treatment = "";

  @Column(nullable = false)
  public String supplyClassification = "";

  @Column(nullable = false, precision = 7, scale = 4)
  public BigDecimal rate = BigDecimal.ZERO;

  @Column(nullable = false)
  public LocalDate effectiveFrom;

  @Column(nullable = true)
  public LocalDate effectiveTo;
}
