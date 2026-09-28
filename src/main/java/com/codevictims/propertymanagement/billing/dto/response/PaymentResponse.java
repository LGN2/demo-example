package com.codevictims.propertymanagement.billing.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record PaymentResponse(
    Long id,
    long version,
    Instant createdAt,
    Long leaseId,
    BigDecimal amount,
    String method,
    String reference,
    LocalDate effectiveDate,
    String idempotencyKey,
    LocalDate reversedOn,
    String reversalReason) {}
