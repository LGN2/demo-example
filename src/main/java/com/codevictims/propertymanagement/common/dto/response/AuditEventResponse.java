package com.codevictims.propertymanagement.common.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record AuditEventResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    String resourceType,
    Long resourceId,
    Long actorId,
    String action,
    String note) {}
