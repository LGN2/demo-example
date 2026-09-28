package com.codevictims.propertymanagement.tenancy.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record MunicipalityRequest(
    @NotBlank @Size(max = 255) String municipalityStatus,
    @Size(max = 255) String municipalityAuthority,
    @Size(max = 255) String municipalityReference,
    @NotNull BigDecimal municipalityFee) implements RequestDto {}
