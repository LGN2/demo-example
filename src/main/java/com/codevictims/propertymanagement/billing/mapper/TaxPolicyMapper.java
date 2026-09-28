package com.codevictims.propertymanagement.billing.mapper;
import com.codevictims.propertymanagement.billing.entity.TaxPolicy;
import com.codevictims.propertymanagement.billing.dto.response.TaxPolicyResponse;
public final class TaxPolicyMapper {
  private TaxPolicyMapper() {}
  public static TaxPolicyResponse toResponse(TaxPolicy entity) {
    if (entity == null) return null;
    return new TaxPolicyResponse(entity.id, entity.version, entity.createdAt, entity.buildingId, entity.treatment, entity.supplyClassification, entity.rate, entity.effectiveFrom, entity.effectiveTo);
  }
}
