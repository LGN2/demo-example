package com.codevictims.propertymanagement.maintenance.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record VendorRequest(
    @NotNull @Positive Long buildingId,
    @NotNull @Positive Long userId,
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(max = 255) String categories,
    @NotNull BigDecimal hourlyRate)
    implements RequestDto {}
