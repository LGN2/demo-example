package com.codevictims.propertymanagement.maintenance.mapper;
import com.codevictims.propertymanagement.maintenance.entity.VendorProfile;
import com.codevictims.propertymanagement.maintenance.dto.response.VendorProfileResponse;
public final class VendorProfileMapper {
  private VendorProfileMapper() {}
  public static VendorProfileResponse toResponse(VendorProfile entity) {
    if (entity == null) return null;
    return new VendorProfileResponse(entity.id, entity.version, entity.createdAt, entity.buildingId, entity.userId, entity.name, entity.categories, entity.hourlyRate);
  }
}
