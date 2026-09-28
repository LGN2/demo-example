package com.codevictims.propertymanagement.billing.entity;

import com.codevictims.propertymanagement.common.entity.Row;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "cheque")
public class Cheque extends Row {
  @Column(nullable = false)
  public Long leaseId;

  @Column(nullable = false)
  public String chequeNumber = "";

  @Column(nullable = false)
  public String bank = "";

  @Column(nullable = false)
  public LocalDate chequeDate;

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal amount = BigDecimal.ZERO;

  @Column(nullable = false, length = 40)
  public String status = "";

  @Column(nullable = true)
  public Long paymentId;
}
