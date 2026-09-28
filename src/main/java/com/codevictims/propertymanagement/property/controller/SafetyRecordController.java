package com.codevictims.propertymanagement.property.controller;

import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.property.dto.request.SafetyRequest;
import com.codevictims.propertymanagement.property.dto.response.SafetyRecordResponse;
import com.codevictims.propertymanagement.property.entity.SafetyRecord;
import com.codevictims.propertymanagement.property.mapper.SafetyRecordMapper;
import com.codevictims.propertymanagement.property.service.OperationsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/safety")
public class SafetyRecordController {
  private final OperationsService service;
  private final ObjectMapper json;

  public SafetyRecordController(OperationsService service, ObjectMapper json) {
    this.service = service;
    this.json = json;
  }

  @GetMapping
  public PageSlice<SafetyRecordResponse> list(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    var rows = service.list("safety", buildingId).stream().map(SafetyRecord.class::cast).toList();
    return PageSlice.of(
            rows,
            page,
            size,
            q,
            r -> {
              try {
                return json.writeValueAsString(r);
              } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new IllegalStateException(e);
              }
            })
        .map(SafetyRecordMapper::toResponse);
  }

  @GetMapping("/{id}")
  public SafetyRecordResponse get(@PathVariable Long id) {
    return SafetyRecordMapper.toResponse((SafetyRecord) service.get("safety", id));
  }

  @PostMapping
  public SafetyRecordResponse create(@Valid @RequestBody SafetyRequest request) {
    return SafetyRecordMapper.toResponse(
        (SafetyRecord) service.save("safety", null, RequestMapper.toInput(request)));
  }

  @PutMapping("/{id}")
  public SafetyRecordResponse update(
      @PathVariable Long id, @Valid @RequestBody SafetyRequest request) {
    return SafetyRecordMapper.toResponse(
        (SafetyRecord) service.save("safety", id, RequestMapper.toInput(request)));
  }
}
