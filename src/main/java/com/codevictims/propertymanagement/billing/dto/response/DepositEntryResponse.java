package com.codevictims.propertymanagement.billing.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record DepositEntryResponse(
    Long id,
    long version,
    Instant createdAt,
    Long leaseId,
    String kind,
    BigDecimal amount,
    LocalDate effectiveDate,
    String reason,
    String idempotencyKey,
    Long reversesId) {}
