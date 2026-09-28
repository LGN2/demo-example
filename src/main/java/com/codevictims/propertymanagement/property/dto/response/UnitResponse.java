package com.codevictims.propertymanagement.property.dto.response;

import java.math.BigDecimal;
import java.time.*;

public record UnitResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    String code,
    String floorName,
    BigDecimal size,
    String kind,
    String availability,
    BigDecimal marketRent,
    LocalDate vacancySince,
    String listing) {}
