package com.codevictims.propertymanagement.property.mapper;
import com.codevictims.propertymanagement.property.entity.Building;
import com.codevictims.propertymanagement.property.dto.response.BuildingResponse;
public final class BuildingMapper {
  private BuildingMapper() {}
  public static BuildingResponse toResponse(Building entity) {
    if (entity == null) return null;
    return new BuildingResponse(entity.id, entity.version, entity.createdAt, entity.ownerId, entity.name, entity.wilayat, entity.address, entity.investmentValue, entity.reminderDays, entity.seasonalStart, entity.seasonalEnd);
  }
}
