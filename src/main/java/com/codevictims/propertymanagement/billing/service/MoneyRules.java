package com.codevictims.propertymanagement.billing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** Pure allocation rules, independent of HTTP and persistence. Caller orders dues oldest first. */
public final class MoneyRules {
  private MoneyRules() {}

  public record Balance(long dueId, BigDecimal remaining) {}

  public record Part(long dueId, BigDecimal amount) {}

  public static List<Part> allocate(BigDecimal requested, List<Balance> dues) {
    BigDecimal amount = requested.setScale(3, RoundingMode.UNNECESSARY);
    if (amount.signum() <= 0) throw new IllegalArgumentException("INVALID_AMOUNT");
    BigDecimal total =
        dues.stream().map(Balance::remaining).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (amount.compareTo(total) > 0) throw new IllegalArgumentException("OVERPAYMENT");
    List<Part> parts = new ArrayList<>();
    for (Balance due : dues) {
      BigDecimal allocated = amount.min(due.remaining());
      if (allocated.signum() > 0) parts.add(new Part(due.dueId(), allocated));
      amount = amount.subtract(allocated);
      if (amount.signum() == 0) break;
    }
    return List.copyOf(parts);
  }

  public static boolean settledOn(
      java.time.LocalDate paid, java.time.LocalDate reversed, java.time.LocalDate asOf) {
    return !paid.isAfter(asOf) && (reversed == null || reversed.isAfter(asOf));
  }
}
