package com.codevictims.propertymanagement.tenancy.controller;

import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.property.service.OperationsService;
import com.codevictims.propertymanagement.tenancy.dto.request.LeadRequest;
import com.codevictims.propertymanagement.tenancy.dto.response.LeadResponse;
import com.codevictims.propertymanagement.tenancy.entity.Lead;
import com.codevictims.propertymanagement.tenancy.mapper.LeadMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/leads")
public class LeadController {
  private final OperationsService service;
  private final ObjectMapper json;

  public LeadController(OperationsService service, ObjectMapper json) {
    this.service = service;
    this.json = json;
  }

  @GetMapping
  public PageSlice<LeadResponse> list(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    var rows = service.list("leads", buildingId).stream().map(Lead.class::cast).toList();
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
        .map(LeadMapper::toResponse);
  }

  @GetMapping("/{id}")
  public LeadResponse get(@PathVariable Long id) {
    return LeadMapper.toResponse((Lead) service.get("leads", id));
  }

  @PostMapping
  public LeadResponse create(@Valid @RequestBody LeadRequest request) {
    return LeadMapper.toResponse(
        (Lead) service.save("leads", null, RequestMapper.toInput(request)));
  }

  @PutMapping("/{id}")
  public LeadResponse update(@PathVariable Long id, @Valid @RequestBody LeadRequest request) {
    return LeadMapper.toResponse((Lead) service.save("leads", id, RequestMapper.toInput(request)));
  }
}
