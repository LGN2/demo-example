package com.codevictims.propertymanagement.billing.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record TaxPolicyRequest(
    @NotNull @Positive Long buildingId,
    @NotBlank @Size(max = 255) String treatment,
    @NotBlank @Size(max = 255) String supplyClassification,
    @NotNull BigDecimal rate,
    @NotNull
        @org.springframework.format.annotation.DateTimeFormat(
            iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
        LocalDate effectiveFrom,
    @org.springframework.format.annotation.DateTimeFormat(
            iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
        LocalDate effectiveTo)
    implements RequestDto {}
