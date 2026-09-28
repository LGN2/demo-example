package com.codevictims.propertymanagement.tenancy.mapper;
import com.codevictims.propertymanagement.tenancy.entity.Lead;
import com.codevictims.propertymanagement.tenancy.dto.response.LeadResponse;
public final class LeadMapper {
  private LeadMapper() {}
  public static LeadResponse toResponse(Lead entity) {
    if (entity == null) return null;
    return new LeadResponse(entity.id, entity.version, entity.createdAt, entity.buildingId, entity.unitId, entity.name, entity.phone, entity.status, entity.viewingAt, entity.followUpDate, entity.notes);
  }
}
