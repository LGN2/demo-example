package com.codevictims.propertymanagement.billing.mapper;
import com.codevictims.propertymanagement.billing.entity.FollowUp;
import com.codevictims.propertymanagement.billing.dto.response.FollowUpResponse;
public final class FollowUpMapper {
  private FollowUpMapper() {}
  public static FollowUpResponse toResponse(FollowUp entity) {
    if (entity == null) return null;
    return new FollowUpResponse(entity.id, entity.version, entity.createdAt, entity.leaseId, entity.note, entity.nextDate);
  }
}
