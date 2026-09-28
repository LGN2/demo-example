package com.codevictims.propertymanagement.property.mapper;

import com.codevictims.propertymanagement.property.dto.response.MeterResponse;
import com.codevictims.propertymanagement.property.entity.Meter;

public final class MeterMapper {
  private MeterMapper() {}

  public static MeterResponse toResponse(Meter entity) {
    if (entity == null) return null;
    return new MeterResponse(
        entity.id,
        entity.version,
        entity.createdAt,
        entity.buildingId,
        entity.unitId,
        entity.accountNumber,
        entity.kind,
        entity.responsibility,
        entity.commonArea,
        entity.alertThreshold);
  }
}
