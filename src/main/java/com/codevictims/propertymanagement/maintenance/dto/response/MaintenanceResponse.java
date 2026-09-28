package com.codevictims.propertymanagement.maintenance.dto.response;

import java.time.*;

public record MaintenanceResponse(
    Long id,
    long version,
    Instant createdAt,
    boolean seasonalPriority,
    Long buildingId,
    Long unitId,
    Long tenantId,
    String description,
    String category,
    boolean urgent,
    String status,
    Long assignedTo,
    String approvedSummary,
    String proposedSummary,
    String proposedCategory,
    String aiStatus,
    Instant resolvedAt,
    Instant closedAt) {}
