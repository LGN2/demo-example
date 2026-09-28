package com.codevictims.propertymanagement.maintenance.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record PreventiveTaskRequest(
    @NotNull @Positive Long buildingId,
    @Positive Long unitId,
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 255) String category,
     Integer intervalDays,
    @NotNull @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate nextDue,
     Boolean enabled) implements RequestDto {}
