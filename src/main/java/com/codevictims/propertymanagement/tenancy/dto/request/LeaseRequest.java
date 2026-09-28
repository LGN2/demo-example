package com.codevictims.propertymanagement.tenancy.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record LeaseRequest(
    @NotNull @Positive Long unitId,
    @NotNull @Positive Long tenantId,
    @NotNull @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
     Integer months,
    @NotNull BigDecimal rent,
    @NotNull BigDecimal deposit,
    @NotNull @Positive Long taxPolicyId) implements RequestDto {}
