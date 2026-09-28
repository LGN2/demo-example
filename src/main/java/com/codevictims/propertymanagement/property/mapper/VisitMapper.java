package com.codevictims.propertymanagement.property.mapper;
import com.codevictims.propertymanagement.property.entity.Visit;
import com.codevictims.propertymanagement.property.dto.response.VisitResponse;
public final class VisitMapper {
  private VisitMapper() {}
  public static VisitResponse toResponse(Visit entity) {
    if (entity == null) return null;
    return new VisitResponse(entity.id, entity.version, entity.createdAt, entity.buildingId, entity.unitId, entity.visitorName, entity.purpose, entity.checkIn, entity.checkOut);
  }
}
