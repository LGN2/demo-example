package com.codevictims.propertymanagement.billing.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record FollowUpRequest(
    @NotBlank @Size(max = 2000) String note,
    @org.springframework.format.annotation.DateTimeFormat(
            iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
        LocalDate nextDate)
    implements RequestDto {}
