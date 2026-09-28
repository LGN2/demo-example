package com.codevictims.propertymanagement.billing.dto.response;
import java.math.BigDecimal;
import java.time.*;
public record ExpenseResponse(
    Long id,
    long version,
    Instant createdAt,
    Long buildingId,
    Long unitId,
    BigDecimal amount,
    LocalDate expenseDate,
    String category,
    String description,
    LocalDate reversedOn,
    String reversalReason) {}
