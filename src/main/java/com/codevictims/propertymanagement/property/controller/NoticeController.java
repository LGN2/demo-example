package com.codevictims.propertymanagement.property.controller;

import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.property.dto.request.NoticeRequest;
import com.codevictims.propertymanagement.property.dto.response.NoticeResponse;
import com.codevictims.propertymanagement.property.entity.Notice;
import com.codevictims.propertymanagement.property.mapper.NoticeMapper;
import com.codevictims.propertymanagement.property.service.OperationsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/notices")
public class NoticeController {
  private final OperationsService service;
  private final ObjectMapper json;

  public NoticeController(OperationsService service, ObjectMapper json) {
    this.service = service;
    this.json = json;
  }

  @GetMapping
  public PageSlice<NoticeResponse> list(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    var rows = service.list("notices", buildingId).stream().map(Notice.class::cast).toList();
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
        .map(NoticeMapper::toResponse);
  }

  @GetMapping("/{id}")
  public NoticeResponse get(@PathVariable Long id) {
    return NoticeMapper.toResponse((Notice) service.get("notices", id));
  }

  @PostMapping
  public NoticeResponse create(@Valid @RequestBody NoticeRequest request) {
    return NoticeMapper.toResponse(
        (Notice) service.save("notices", null, RequestMapper.toInput(request)));
  }

  @PutMapping("/{id}")
  public NoticeResponse update(@PathVariable Long id, @Valid @RequestBody NoticeRequest request) {
    return NoticeMapper.toResponse(
        (Notice) service.save("notices", id, RequestMapper.toInput(request)));
  }
}
