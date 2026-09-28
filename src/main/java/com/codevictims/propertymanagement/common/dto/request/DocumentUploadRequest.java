package com.codevictims.propertymanagement.common.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record DocumentUploadRequest(
    @NotNull @Positive Long buildingId,
    @Positive Long unitId,
    @Positive Long tenantId,
    @Positive Long maintenanceId,
    @Positive Long readingId,
    @NotBlank @Size(max = 255) String kind,
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate expiryDate) implements RequestDto {}
