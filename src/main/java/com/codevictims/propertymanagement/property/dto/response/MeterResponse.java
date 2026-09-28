package com.codevictims.propertymanagement.property.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record MeterResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    Long unitId,
    String accountNumber,
    String kind,
    String responsibility,
    boolean commonArea,
    BigDecimal alertThreshold) {}
