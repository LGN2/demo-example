package com.codevictims.propertymanagement.tenancy.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record TenantRequest(
    @NotNull @Positive Long buildingId,
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(max = 255) String kind,
    @NotBlank @Size(max = 255) String phone,
    @Size(max = 255) String email,
    @Size(max = 255) String emergencyContact,
    @Positive Long accountId)
    implements RequestDto {}
