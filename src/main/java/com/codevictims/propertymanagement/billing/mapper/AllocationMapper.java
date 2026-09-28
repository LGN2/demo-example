package com.codevictims.propertymanagement.billing.mapper;

import com.codevictims.propertymanagement.billing.dto.response.AllocationResponse;
import com.codevictims.propertymanagement.billing.entity.Allocation;

public final class AllocationMapper {
  private AllocationMapper() {}

  public static AllocationResponse toResponse(Allocation entity) {
    if (entity == null) return null;
    return new AllocationResponse(
        entity.id, entity.version, entity.createdAt, entity.paymentId, entity.dueId, entity.amount);
  }
}
