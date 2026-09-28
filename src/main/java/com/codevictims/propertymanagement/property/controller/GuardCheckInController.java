package com.codevictims.propertymanagement.property.controller;

import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.property.dto.request.GuardCheckInRequest;
import com.codevictims.propertymanagement.property.dto.response.GuardCheckInResponse;
import com.codevictims.propertymanagement.property.entity.GuardCheckIn;
import com.codevictims.propertymanagement.property.mapper.GuardCheckInMapper;
import com.codevictims.propertymanagement.property.service.OperationsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/checkins")
public class GuardCheckInController {
  private final OperationsService service;
  private final ObjectMapper json;

  public GuardCheckInController(OperationsService service, ObjectMapper json) {
    this.service = service;
    this.json = json;
  }

  @GetMapping
  public PageSlice<GuardCheckInResponse> list(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    var rows = service.list("checkins", buildingId).stream().map(GuardCheckIn.class::cast).toList();
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
        .map(GuardCheckInMapper::toResponse);
  }

  @GetMapping("/{id}")
  public GuardCheckInResponse get(@PathVariable Long id) {
    return GuardCheckInMapper.toResponse((GuardCheckIn) service.get("checkins", id));
  }

  @PostMapping
  public GuardCheckInResponse create(@Valid @RequestBody GuardCheckInRequest request) {
    return GuardCheckInMapper.toResponse(
        (GuardCheckIn) service.save("checkins", null, RequestMapper.toInput(request)));
  }
}
