package com.codevictims.propertymanagement.property.controller;
import com.codevictims.propertymanagement.property.entity.Visit;
import com.codevictims.propertymanagement.property.dto.request.VisitRequest;
import com.codevictims.propertymanagement.property.dto.response.VisitResponse;
import com.codevictims.propertymanagement.property.mapper.VisitMapper;
import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.property.service.OperationsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/operations/visits")
public class VisitController {
  private final OperationsService service;
  private final ObjectMapper json;
  public VisitController(OperationsService service, ObjectMapper json) { this.service=service; this.json=json; }
  @GetMapping
  public PageSlice<VisitResponse> list(@RequestParam(required=false) Long buildingId,
      @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
      @RequestParam(defaultValue="") String q) {
    var rows=service.list("visits", buildingId).stream().map(Visit.class::cast).toList();
    return PageSlice.of(rows,page,size,q,r->{try {return json.writeValueAsString(r);}
      catch (com.fasterxml.jackson.core.JsonProcessingException e) {throw new IllegalStateException(e);}}).map(VisitMapper::toResponse);
  }
  @GetMapping("/{id}")
  public VisitResponse get(@PathVariable Long id) {return VisitMapper.toResponse((Visit)service.get("visits",id));}
  @PostMapping
  public VisitResponse create(@Valid @RequestBody VisitRequest request) {return VisitMapper.toResponse((Visit)service.save("visits",null,RequestMapper.toInput(request)));}
  @PostMapping("/{id}/checkout")
  public VisitResponse checkout(@PathVariable Long id) {return VisitMapper.toResponse(service.checkout(id));}
}
