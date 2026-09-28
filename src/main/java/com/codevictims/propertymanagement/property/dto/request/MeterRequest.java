package com.codevictims.propertymanagement.property.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record MeterRequest(
    @NotNull @Positive Long buildingId,
    @Positive Long unitId,
    @NotBlank @Size(max = 255) String accountNumber,
    @NotBlank @Size(max = 255) String kind,
    @NotBlank @Size(max = 255) String responsibility,
    Boolean commonArea,
    @NotNull BigDecimal alertThreshold)
    implements RequestDto {}
