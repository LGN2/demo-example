package com.codevictims.propertymanagement.billing.mapper;
import com.codevictims.propertymanagement.billing.entity.Due;
import com.codevictims.propertymanagement.billing.dto.response.DueResponse;
public final class DueMapper {
  private DueMapper() {}
  public static DueResponse toResponse(Due entity) {
    if (entity == null) return null;
    return new DueResponse(entity.id, entity.version, entity.createdAt, entity.leaseId, entity.dueDate, entity.rentAmount, entity.taxAmount, entity.amount, entity.cancelled, entity.cancelledOn);
  }
}
