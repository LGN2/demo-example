package com.codevictims.propertymanagement.common.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record DocumentResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    Long unitId,
    Long tenantId,
    Long maintenanceId,
    Long readingId,
    String kind,
    String filename,
    String contentType,
    Long sizeBytes,
    String scanStatus,
    LocalDate expiryDate) {}
