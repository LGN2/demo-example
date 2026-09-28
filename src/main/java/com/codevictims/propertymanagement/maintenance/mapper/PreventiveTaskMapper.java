package com.codevictims.propertymanagement.maintenance.mapper;
import com.codevictims.propertymanagement.maintenance.entity.PreventiveTask;
import com.codevictims.propertymanagement.maintenance.dto.response.PreventiveTaskResponse;
public final class PreventiveTaskMapper {
  private PreventiveTaskMapper() {}
  public static PreventiveTaskResponse toResponse(PreventiveTask entity) {
    if (entity == null) return null;
    return new PreventiveTaskResponse(entity.id, entity.version, entity.createdAt, entity.seasonalPriority, entity.buildingId, entity.unitId, entity.title, entity.category, entity.intervalDays, entity.nextDue, entity.lastCompleted, entity.enabled);
  }
}
