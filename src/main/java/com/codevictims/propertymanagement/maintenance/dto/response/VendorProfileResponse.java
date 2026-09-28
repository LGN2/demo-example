package com.codevictims.propertymanagement.maintenance.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record VendorProfileResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    Long userId,
    String name,
    String categories,
    BigDecimal hourlyRate) {}
