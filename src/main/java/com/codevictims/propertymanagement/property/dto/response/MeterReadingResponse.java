package com.codevictims.propertymanagement.property.dto.response;

import java.math.BigDecimal;
import java.time.*;

public record MeterReadingResponse(
    Long id,
    long version,
    Instant createdAt,
    Long meterId,
    LocalDate readingDate,
    BigDecimal value,
    String kind,
    String observation) {}
