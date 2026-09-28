package com.codevictims.propertymanagement.property.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record VisitRequest(
    @NotNull @Positive Long buildingId,
    @Positive Long unitId,
    @NotBlank @Size(max = 255) String visitorName,
    @NotBlank @Size(max = 255) String purpose) implements RequestDto {}
