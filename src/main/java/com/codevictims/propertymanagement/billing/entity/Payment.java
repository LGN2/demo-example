package com.codevictims.propertymanagement.billing.entity;

import com.codevictims.propertymanagement.common.entity.Row;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "payment")
public class Payment extends Row {
  @Column(nullable = false)
  public Long leaseId;

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal amount = BigDecimal.ZERO;

  @Column(nullable = false, length = 40)
  public String method = "";

  @Column(nullable = false)
  public String reference = "";

  @Column(nullable = false)
  public LocalDate effectiveDate;

  @Column(nullable = false)
  public String idempotencyKey = "";

  @Column(nullable = true)
  public LocalDate reversedOn;

  @Column(nullable = false)
  public String reversalReason = "";
}
