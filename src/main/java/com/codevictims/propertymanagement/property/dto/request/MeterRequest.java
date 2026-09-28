package com.codevictims.propertymanagement.property.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record MeterRequest(
    @NotNull @Positive Long buildingId,
    @Positive Long unitId,
    @NotBlank @Size(max = 255) String accountNumber,
    @NotBlank @Size(max = 255) String kind,
    @NotBlank @Size(max = 255) String responsibility,
     Boolean commonArea,
    @NotNull BigDecimal alertThreshold) implements RequestDto {}
