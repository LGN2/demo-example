package com.codevictims.propertymanagement.property.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record BuildingResponse(
    Long id,
    long version,
    Instant createdAt,
    Long ownerId,
    String name,
    String wilayat,
    String address,
    BigDecimal investmentValue,
    int reminderDays,
    int seasonalStart,
    int seasonalEnd) {}
