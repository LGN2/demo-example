package com.codevictims.propertymanagement.common.mapper;
import java.util.*;
import java.time.temporal.TemporalAccessor;
import com.codevictims.propertymanagement.common.dto.*;
/** Preserves absent optional fields while translating typed transport values to domain validation. */
public final class RequestMapper {
  private RequestMapper() {}
  public static Input toInput(RequestDto request) {
    Map<String,Object> values = new LinkedHashMap<>();
    try {
      for (var field : request.getClass().getRecordComponents()) {
        Object value = field.getAccessor().invoke(request);
        if (value != null) values.put(field.getName(), value instanceof TemporalAccessor ? value.toString() : value);
      }
    } catch (ReflectiveOperationException e) { throw new IllegalStateException("Cannot map declared request", e); }
    return new Input(values);
  }
}
