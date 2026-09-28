package com.codevictims.propertymanagement.property.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record VisitRequest(
    @NotNull @Positive Long buildingId,
    @Positive Long unitId,
    @NotBlank @Size(max = 255) String visitorName,
    @NotBlank @Size(max = 255) String purpose)
    implements RequestDto {}
