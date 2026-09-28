package com.codevictims.propertymanagement.billing.dto.response;

import java.math.BigDecimal;
import java.time.*;

public record TaxPolicyResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    String treatment,
    String supplyClassification,
    BigDecimal rate,
    LocalDate effectiveFrom,
    LocalDate effectiveTo) {}
