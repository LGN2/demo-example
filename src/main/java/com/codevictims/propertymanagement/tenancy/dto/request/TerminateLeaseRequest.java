package com.codevictims.propertymanagement.tenancy.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record TerminateLeaseRequest(
    @NotNull
        @org.springframework.format.annotation.DateTimeFormat(
            iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
        LocalDate terminatedOn,
    @NotBlank @Size(max = 255) String reason)
    implements RequestDto {}
