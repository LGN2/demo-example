package com.codevictims.propertymanagement.billing.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record DepositRequest(
    @NotBlank @Size(max = 255) String kind,
    @NotNull BigDecimal amount,
    @NotNull @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate effectiveDate,
    @NotBlank @Size(max = 255) String reason,
    @NotBlank @Size(max = 255) String idempotencyKey,
    @Positive Long reversesId) implements RequestDto {}
