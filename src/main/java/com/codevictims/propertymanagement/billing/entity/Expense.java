package com.codevictims.propertymanagement.billing.entity;

import com.codevictims.propertymanagement.common.entity.Row;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "expense")
public class Expense extends Row {
  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = true)
  public Long unitId;

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal amount = BigDecimal.ZERO;

  @Column(nullable = false)
  public LocalDate expenseDate;

  @Column(nullable = false, length = 40)
  public String category = "";

  @Column(nullable = false)
  public String description = "";

  @Column(nullable = true)
  public LocalDate reversedOn;

  @Column(nullable = false)
  public String reversalReason = "";
}
