package com.codevictims.propertymanagement.property.dto.response;

import java.time.*;

public record VisitResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    Long unitId,
    String visitorName,
    String purpose,
    Instant checkIn,
    Instant checkOut) {}
