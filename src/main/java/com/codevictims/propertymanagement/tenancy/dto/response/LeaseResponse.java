package com.codevictims.propertymanagement.tenancy.dto.response;

import java.math.BigDecimal;
import java.time.*;

public record LeaseResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    Long unitId,
    Long tenantId,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal rent,
    BigDecimal deposit,
    String status,
    String taxTreatment,
    BigDecimal taxRate,
    String supplyClassification,
    boolean ownerTaxRegistered,
    String municipalityStatus,
    String municipalityAuthority,
    String municipalityReference,
    BigDecimal municipalityFee,
    Long previousLeaseId,
    LocalDate terminatedOn) {}
