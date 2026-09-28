package com.codevictims.propertymanagement.property.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record ParkingRequest(
    @NotNull @Positive Long buildingId,
    @NotNull @Positive Long unitId,
    @NotBlank @Size(max = 255) String space,
    @NotBlank @Size(max = 255) String vehicle) implements RequestDto {}
