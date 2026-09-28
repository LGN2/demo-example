package com.codevictims.propertymanagement.maintenance.mapper;
import com.codevictims.propertymanagement.maintenance.entity.Maintenance;
import com.codevictims.propertymanagement.maintenance.dto.response.MaintenanceResponse;
public final class MaintenanceMapper {
  private MaintenanceMapper() {}
  public static MaintenanceResponse toResponse(Maintenance entity) {
    if (entity == null) return null;
    return new MaintenanceResponse(entity.id, entity.version, entity.createdAt, entity.seasonalPriority, entity.buildingId, entity.unitId, entity.tenantId, entity.description, entity.category, entity.urgent, entity.status, entity.assignedTo, entity.approvedSummary, entity.proposedSummary, entity.proposedCategory, entity.aiStatus, entity.resolvedAt, entity.closedAt);
  }
}
