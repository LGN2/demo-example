package com.codevictims.propertymanagement.property.mapper;

import com.codevictims.propertymanagement.property.dto.response.SafetyRecordResponse;
import com.codevictims.propertymanagement.property.entity.SafetyRecord;

public final class SafetyRecordMapper {
  private SafetyRecordMapper() {}

  public static SafetyRecordResponse toResponse(SafetyRecord entity) {
    if (entity == null) return null;
    return new SafetyRecordResponse(
        entity.id,
        entity.version,
        entity.createdAt,
        entity.buildingId,
        entity.kind,
        entity.reference,
        entity.expiryDate,
        entity.notes);
  }
}
