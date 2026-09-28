package com.codevictims.propertymanagement.billing.dto.response;

import java.math.BigDecimal;
import java.time.*;

public record AllocationResponse(
    Long id, long version, Instant createdAt, Long paymentId, Long dueId, BigDecimal amount) {}
