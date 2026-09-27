package om.bayt.api;

import java.math.*;
import java.time.*;
import java.util.*;

/** Explicit field extraction prevents request objects from changing protected entity fields. */
public class Input {
  private final Map<String, Object> values;

  public Input(Map<String, Object> values) {
    this.values = Objects.requireNonNull(values);
  }

  public boolean has(String key) {
    return values.containsKey(key)
        && values.get(key) != null
        && !values.get(key).toString().isBlank();
  }

  public String optional(String key) {
    return has(key) ? text(key, 255) : "";
  }

  public String text(String key, int max) {
    Object raw = values.get(key);
    if (!(raw instanceof String s) || s.isBlank() || s.length() > max)
      throw ApiException.invalid("INVALID_INPUT");
    return s.trim();
  }

  public String choice(String key, String... allowed) {
    String s = text(key, 40);
    if (!Arrays.asList(allowed).contains(s)) throw ApiException.invalid("INVALID_INPUT");
    return s;
  }

  public Long id(String key) {
    try {
      long n = Long.parseLong(values.get(key).toString());
      if (n <= 0) throw new NumberFormatException();
      return n;
    } catch (Exception e) {
      throw ApiException.invalid("INVALID_INPUT");
    }
  }

  public Long nullableId(String key) {
    return has(key) ? id(key) : null;
  }

  public int integer(String key, int min, int max, int fallback) {
    if (!has(key)) return fallback;
    try {
      int n = Integer.parseInt(values.get(key).toString());
      if (n < min || n > max) throw new NumberFormatException();
      return n;
    } catch (Exception e) {
      throw ApiException.invalid("INVALID_INPUT");
    }
  }

  public boolean bool(String key) {
    return Boolean.TRUE.equals(values.get(key)) || "true".equals(values.get(key));
  }

  public BigDecimal money(String key, boolean positive) {
    try {
      BigDecimal n =
          new BigDecimal(values.get(key).toString()).setScale(3, RoundingMode.UNNECESSARY);
      if (n.signum() < 0 || (positive && n.signum() == 0) || n.precision() > 15)
        throw new NumberFormatException();
      return n;
    } catch (Exception e) {
      throw ApiException.invalid("INVALID_AMOUNT");
    }
  }

  public BigDecimal rate(String key) {
    try {
      BigDecimal n =
          new BigDecimal(values.get(key).toString()).setScale(4, RoundingMode.UNNECESSARY);
      if (n.signum() < 0 || n.compareTo(BigDecimal.ONE) > 0) throw new NumberFormatException();
      return n;
    } catch (Exception e) {
      throw ApiException.invalid("INVALID_AMOUNT");
    }
  }

  public LocalDate date(String key) {
    try {
      return LocalDate.parse(text(key, 10));
    } catch (Exception e) {
      throw ApiException.invalid("INVALID_DATE");
    }
  }

  public LocalDate optionalDate(String key) {
    return has(key) ? date(key) : null;
  }

  public Instant instant(String key) {
    try {
      return Instant.parse(text(key, 40));
    } catch (Exception e) {
      throw ApiException.invalid("INVALID_DATE");
    }
  }
}
