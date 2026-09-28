package com.codevictims.propertymanagement.billing.dto.response;
import java.math.BigDecimal;
import java.util.List;
public record DepositLedgerResponse(List<DepositEntryResponse> entries, BigDecimal held) {}
