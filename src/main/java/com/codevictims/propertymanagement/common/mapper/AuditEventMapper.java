package com.codevictims.propertymanagement.common.mapper;

import com.codevictims.propertymanagement.common.dto.response.AuditEventResponse;
import com.codevictims.propertymanagement.common.entity.AuditEvent;

public final class AuditEventMapper {
  private AuditEventMapper() {}

  public static AuditEventResponse toResponse(AuditEvent entity) {
    if (entity == null) return null;
    return new AuditEventResponse(
        entity.id,
        entity.version,
        entity.createdAt,
        entity.buildingId,
        entity.resourceType,
        entity.resourceId,
        entity.actorId,
        entity.action,
        entity.note);
  }
}
