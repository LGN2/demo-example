package com.codevictims.propertymanagement.property.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record ParkingResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    Long unitId,
    String space,
    String vehicle) {}
