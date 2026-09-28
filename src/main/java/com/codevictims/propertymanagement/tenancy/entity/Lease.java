package com.codevictims.propertymanagement.tenancy.entity;

import com.codevictims.propertymanagement.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "lease")
public class Lease extends BaseEntity {
  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = false)
  public Long unitId;

  @Column(nullable = false)
  public Long tenantId;

  @Column(nullable = false)
  public LocalDate startDate;

  @Column(nullable = false)
  public LocalDate endDate;

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal rent = BigDecimal.ZERO;

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal deposit = BigDecimal.ZERO;

  @Column(nullable = false, length = 40)
  public String status = "";

  @Column(nullable = false, length = 40)
  public String taxTreatment = "";

  @Column(nullable = false, precision = 7, scale = 4)
  public BigDecimal taxRate = BigDecimal.ZERO;

  @Column(nullable = false)
  public String supplyClassification = "";

  @Column(nullable = false)
  public boolean ownerTaxRegistered;

  @Column(nullable = false, length = 40)
  public String municipalityStatus = "";

  @Column(nullable = false)
  public String municipalityAuthority = "";

  @Column(nullable = false)
  public String municipalityReference = "";

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal municipalityFee = BigDecimal.ZERO;

  @Column(nullable = true)
  public Long previousLeaseId;

  @Column(nullable = true)
  public LocalDate terminatedOn;
}
