package com.codevictims.propertymanagement.tenancy.mapper;
import com.codevictims.propertymanagement.tenancy.entity.Lease;
import com.codevictims.propertymanagement.tenancy.dto.response.LeaseResponse;
public final class LeaseMapper {
  private LeaseMapper() {}
  public static LeaseResponse toResponse(Lease entity) {
    if (entity == null) return null;
    return new LeaseResponse(entity.id, entity.version, entity.createdAt, entity.buildingId, entity.unitId, entity.tenantId, entity.startDate, entity.endDate, entity.rent, entity.deposit, entity.status, entity.taxTreatment, entity.taxRate, entity.supplyClassification, entity.ownerTaxRegistered, entity.municipalityStatus, entity.municipalityAuthority, entity.municipalityReference, entity.municipalityFee, entity.previousLeaseId, entity.terminatedOn);
  }
}
