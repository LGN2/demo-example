package com.codevictims.propertymanagement.property.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record SafetyRequest(
    @NotNull @Positive Long buildingId,
    @NotBlank @Size(max = 255) String kind,
    @NotBlank @Size(max = 255) String reference,
    @NotNull
        @org.springframework.format.annotation.DateTimeFormat(
            iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
        LocalDate expiryDate,
    @Size(max = 2000) String notes)
    implements RequestDto {}
