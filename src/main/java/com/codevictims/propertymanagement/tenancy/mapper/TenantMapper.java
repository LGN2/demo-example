package com.codevictims.propertymanagement.tenancy.mapper;
import com.codevictims.propertymanagement.tenancy.entity.Tenant;
import com.codevictims.propertymanagement.tenancy.dto.response.TenantResponse;
public final class TenantMapper {
  private TenantMapper() {}
  public static TenantResponse toResponse(Tenant entity) {
    if (entity == null) return null;
    return new TenantResponse(entity.id, entity.version, entity.createdAt, entity.buildingId, entity.accountId, entity.name, entity.kind, entity.email, entity.phone, entity.emergencyContact);
  }
}
