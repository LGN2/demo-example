package com.codevictims.propertymanagement.billing.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record ChequeRequest(
    @NotBlank @Size(max = 255) String chequeNumber,
    @NotBlank @Size(max = 255) String bank,
    @NotNull
        @org.springframework.format.annotation.DateTimeFormat(
            iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
        LocalDate chequeDate,
    @NotNull BigDecimal amount)
    implements RequestDto {}
