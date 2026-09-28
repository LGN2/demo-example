package com.codevictims.propertymanagement.billing.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record FollowUpResponse(
    Long id,
    long version,
    Instant createdAt,
    Long leaseId,
    String note,
    LocalDate nextDate) {}
