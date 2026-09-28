package com.codevictims.propertymanagement.maintenance.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record PreventiveTaskResponse(
    Long id,
    long version,
    Instant createdAt,
    boolean seasonalPriority,
    Long buildingId,
    Long unitId,
    String title,
    String category,
    int intervalDays,
    LocalDate nextDue,
    LocalDate lastCompleted,
    boolean enabled) {}
