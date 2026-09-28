package com.codevictims.propertymanagement.maintenance.dto.request;
import java.math.BigDecimal;
import java.time.*;
import jakarta.validation.constraints.*;
import com.codevictims.propertymanagement.common.dto.RequestDto;
/** Editable input only. Ownership and ledger state are assigned by the service. */
public record MaintenanceStatusRequest(
    @NotBlank @Size(max = 255) String status,
    @Positive Long assignedTo,
    @Size(max = 2000) String note) implements RequestDto {}
