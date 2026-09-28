package com.codevictims.propertymanagement.account.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record BuildingAccessRequest(
    @NotNull @Positive Long buildingId, @NotNull @Positive Long userId, Boolean canWrite)
    implements RequestDto {}
