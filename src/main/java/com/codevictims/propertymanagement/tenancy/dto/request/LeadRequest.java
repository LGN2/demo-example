package com.codevictims.propertymanagement.tenancy.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record LeadRequest(
    @NotNull @Positive Long buildingId,
    @NotNull @Positive Long unitId,
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(max = 255) String phone,
    @NotBlank @Size(max = 255) String status,
    @org.springframework.format.annotation.DateTimeFormat(
            iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        Instant viewingAt,
    @org.springframework.format.annotation.DateTimeFormat(
            iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
        LocalDate followUpDate,
    @Size(max = 2000) String notes)
    implements RequestDto {}
