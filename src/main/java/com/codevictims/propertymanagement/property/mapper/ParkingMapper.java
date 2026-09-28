package com.codevictims.propertymanagement.property.mapper;
import com.codevictims.propertymanagement.property.entity.Parking;
import com.codevictims.propertymanagement.property.dto.response.ParkingResponse;
public final class ParkingMapper {
  private ParkingMapper() {}
  public static ParkingResponse toResponse(Parking entity) {
    if (entity == null) return null;
    return new ParkingResponse(entity.id, entity.version, entity.createdAt, entity.buildingId, entity.unitId, entity.space, entity.vehicle);
  }
}
