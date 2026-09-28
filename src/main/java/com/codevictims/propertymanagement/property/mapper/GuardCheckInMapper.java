package com.codevictims.propertymanagement.property.mapper;

import com.codevictims.propertymanagement.property.dto.response.GuardCheckInResponse;
import com.codevictims.propertymanagement.property.entity.GuardCheckIn;

public final class GuardCheckInMapper {
  private GuardCheckInMapper() {}

  public static GuardCheckInResponse toResponse(GuardCheckIn entity) {
    if (entity == null) return null;
    return new GuardCheckInResponse(
        entity.id, entity.version, entity.createdAt, entity.buildingId, entity.userId, entity.note);
  }
}
