package com.codevictims.propertymanagement.property.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record NoticeRequest(
    @NotNull @Positive Long buildingId,
    @NotBlank @Size(max = 255) String titleAr,
    @NotBlank @Size(max = 255) String titleEn,
    @NotBlank @Size(max = 5000) String bodyAr,
    @NotBlank @Size(max = 5000) String bodyEn) implements RequestDto {}
