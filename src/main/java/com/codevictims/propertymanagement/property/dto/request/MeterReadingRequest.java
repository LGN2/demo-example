package com.codevictims.propertymanagement.property.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record MeterReadingRequest(
    @NotNull
        @org.springframework.format.annotation.DateTimeFormat(
            iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
        LocalDate readingDate,
    @NotNull BigDecimal value,
    @NotBlank @Size(max = 255) String kind)
    implements RequestDto {}
