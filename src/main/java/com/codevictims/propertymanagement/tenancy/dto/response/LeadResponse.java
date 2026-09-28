package com.codevictims.propertymanagement.tenancy.dto.response;

import java.time.*;

public record LeadResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    Long unitId,
    String name,
    String phone,
    String status,
    Instant viewingAt,
    LocalDate followUpDate,
    String notes) {}
