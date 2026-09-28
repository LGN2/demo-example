package com.codevictims.propertymanagement.property.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record SafetyRecordResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    String kind,
    String reference,
    LocalDate expiryDate,
    String notes) {}
