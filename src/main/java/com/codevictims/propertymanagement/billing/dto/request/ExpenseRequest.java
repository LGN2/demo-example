package com.codevictims.propertymanagement.billing.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record ExpenseRequest(
    @NotNull @Positive Long buildingId,
    @Positive Long unitId,
    @NotNull BigDecimal amount,
    @NotNull
        @org.springframework.format.annotation.DateTimeFormat(
            iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
        LocalDate expenseDate,
    @NotBlank @Size(max = 255) String category,
    @NotBlank @Size(max = 2000) String description)
    implements RequestDto {}
