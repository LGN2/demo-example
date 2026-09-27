package om.bayt.api;

import java.util.*;
import java.util.function.Function;

public record PageSlice<T>(List<T> items, long total, int page, int size) {
  public static <T> PageSlice<T> of(
      List<T> rows, int page, int size, String q, Function<T, String> search) {
    if (page < 0 || size < 1 || size > 100) throw ApiException.invalid("INVALID_PAGE");
    String term = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    var filtered =
        rows.stream().filter(r -> search.apply(r).toLowerCase(Locale.ROOT).contains(term)).toList();
    int from = (int) Math.min((long) page * size, filtered.size());
    return new PageSlice<>(
        filtered.subList(from, Math.min(from + size, filtered.size())),
        filtered.size(),
        page,
        size);
  }
}
