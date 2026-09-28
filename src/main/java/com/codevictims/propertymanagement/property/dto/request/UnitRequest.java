package com.codevictims.propertymanagement.property.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record UnitRequest(
    @NotNull @Positive Long buildingId,
    @NotBlank @Size(max = 255) String code,
    @NotBlank @Size(max = 255) String floorName,
    @NotNull BigDecimal size,
    @NotBlank @Size(max = 255) String kind,
    @NotBlank @Size(max = 255) String availability,
    @NotNull BigDecimal marketRent,
    @Size(max = 5000) String listing)
    implements RequestDto {}
