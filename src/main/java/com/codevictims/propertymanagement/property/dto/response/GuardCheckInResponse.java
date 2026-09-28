package com.codevictims.propertymanagement.property.dto.response;

import java.time.*;

public record GuardCheckInResponse(
    Long id, long version, Instant createdAt, Long buildingId, Long userId, String note) {}
