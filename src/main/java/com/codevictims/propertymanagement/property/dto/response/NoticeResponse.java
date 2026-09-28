package com.codevictims.propertymanagement.property.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record NoticeResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    String titleAr,
    String titleEn,
    String bodyAr,
    String bodyEn) {}
