package com.codevictims.propertymanagement.property.mapper;

import com.codevictims.propertymanagement.property.dto.response.MeterReadingResponse;
import com.codevictims.propertymanagement.property.entity.MeterReading;

public final class MeterReadingMapper {
  private MeterReadingMapper() {}

  public static MeterReadingResponse toResponse(MeterReading entity) {
    if (entity == null) return null;
    return new MeterReadingResponse(
        entity.id,
        entity.version,
        entity.createdAt,
        entity.meterId,
        entity.readingDate,
        entity.value,
        entity.kind,
        entity.observation);
  }
}
