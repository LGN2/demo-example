package com.codevictims.propertymanagement.property.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record GuardCheckInRequest(@NotNull @Positive Long buildingId, @Size(max = 2000) String note)
    implements RequestDto {}
