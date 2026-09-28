package com.codevictims.propertymanagement.maintenance.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record AiSuggestionRequest(
    @NotBlank @Size(max = 255) String language) implements RequestDto {}
