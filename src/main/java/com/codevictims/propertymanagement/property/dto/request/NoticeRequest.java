package com.codevictims.propertymanagement.property.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record NoticeRequest(
    @NotNull @Positive Long buildingId,
    @NotBlank @Size(max = 255) String titleAr,
    @NotBlank @Size(max = 255) String titleEn,
    @NotBlank @Size(max = 5000) String bodyAr,
    @NotBlank @Size(max = 5000) String bodyEn)
    implements RequestDto {}
