package com.codevictims.propertymanagement.account.mapper;

import com.codevictims.propertymanagement.account.dto.response.BuildingAccessResponse;
import com.codevictims.propertymanagement.account.entity.BuildingAccess;

public final class BuildingAccessMapper {
  private BuildingAccessMapper() {}

  public static BuildingAccessResponse toResponse(BuildingAccess entity) {
    if (entity == null) return null;
    return new BuildingAccessResponse(
        entity.id,
        entity.version,
        entity.createdAt,
        entity.buildingId,
        entity.userId,
        entity.canWrite);
  }
}
