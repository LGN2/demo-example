package com.codevictims.propertymanagement.property.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record UnitRequest(
    @NotNull @Positive Long buildingId,
    @NotBlank @Size(max = 255) String code,
    @NotBlank @Size(max = 255) String floorName,
    @NotNull BigDecimal size,
    @NotBlank @Size(max = 255) String kind,
    @NotBlank @Size(max = 255) String availability,
    @NotNull BigDecimal marketRent,
    @Size(max = 5000) String listing) implements RequestDto {}
