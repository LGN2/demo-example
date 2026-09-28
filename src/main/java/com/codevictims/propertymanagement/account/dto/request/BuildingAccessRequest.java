package com.codevictims.propertymanagement.account.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record BuildingAccessRequest(
    @NotNull @Positive Long buildingId,
    @NotNull @Positive Long userId,
     Boolean canWrite) implements RequestDto {}
