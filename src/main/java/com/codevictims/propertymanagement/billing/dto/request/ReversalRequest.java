package com.codevictims.propertymanagement.billing.dto.request;

import com.codevictims.propertymanagement.common.dto.RequestDto;
import jakarta.validation.constraints.*;
import java.time.*;

/** Editable input only. Ownership and ledger state are assigned by the service. */
public record ReversalRequest(@NotBlank @Size(max = 255) String reason) implements RequestDto {}
