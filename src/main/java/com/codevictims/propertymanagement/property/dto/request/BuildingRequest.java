package com.codevictims.propertymanagement.property.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record BuildingRequest(
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(max = 255) String wilayat,
    @NotBlank @Size(max = 255) String address,
    BigDecimal investmentValue,
    Integer reminderDays,
    Integer seasonalStart,
    Integer seasonalEnd)
    implements RequestDto {}
