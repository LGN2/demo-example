package com.codevictims.propertymanagement.billing.mapper;

import com.codevictims.propertymanagement.billing.dto.response.DepositEntryResponse;
import com.codevictims.propertymanagement.billing.entity.DepositEntry;

public final class DepositEntryMapper {
  private DepositEntryMapper() {}

  public static DepositEntryResponse toResponse(DepositEntry entity) {
    if (entity == null) return null;
    return new DepositEntryResponse(
        entity.id,
        entity.version,
        entity.createdAt,
        entity.leaseId,
        entity.kind,
        entity.amount,
        entity.effectiveDate,
        entity.reason,
        entity.idempotencyKey,
        entity.reversesId);
  }
}
