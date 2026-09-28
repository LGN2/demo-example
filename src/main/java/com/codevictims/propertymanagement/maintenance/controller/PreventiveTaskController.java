package com.codevictims.propertymanagement.maintenance.controller;
import com.codevictims.propertymanagement.maintenance.entity.PreventiveTask;
import com.codevictims.propertymanagement.maintenance.dto.request.PreventiveTaskRequest;
import com.codevictims.propertymanagement.maintenance.dto.response.PreventiveTaskResponse;
import com.codevictims.propertymanagement.maintenance.mapper.PreventiveTaskMapper;
import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.property.service.OperationsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/operations/preventive")
public class PreventiveTaskController {
  private final OperationsService service;
  private final ObjectMapper json;
  public PreventiveTaskController(OperationsService service, ObjectMapper json) { this.service=service; this.json=json; }
  @GetMapping
  public PageSlice<PreventiveTaskResponse> list(@RequestParam(required=false) Long buildingId,
      @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
      @RequestParam(defaultValue="") String q) {
    var rows=service.list("preventive", buildingId).stream().map(PreventiveTask.class::cast).toList();
    return PageSlice.of(rows,page,size,q,r->{try {return json.writeValueAsString(r);}
      catch (com.fasterxml.jackson.core.JsonProcessingException e) {throw new IllegalStateException(e);}}).map(PreventiveTaskMapper::toResponse);
  }
  @GetMapping("/{id}")
  public PreventiveTaskResponse get(@PathVariable Long id) {return PreventiveTaskMapper.toResponse((PreventiveTask)service.get("preventive",id));}
  @PostMapping
  public PreventiveTaskResponse create(@Valid @RequestBody PreventiveTaskRequest request) {return PreventiveTaskMapper.toResponse((PreventiveTask)service.save("preventive",null,RequestMapper.toInput(request)));}
  @PutMapping("/{id}")
  public PreventiveTaskResponse update(@PathVariable Long id,@Valid @RequestBody PreventiveTaskRequest request) {return PreventiveTaskMapper.toResponse((PreventiveTask)service.save("preventive",id,RequestMapper.toInput(request)));}
  @PostMapping("/{id}/complete")
  public PreventiveTaskResponse complete(@PathVariable Long id) {return PreventiveTaskMapper.toResponse(service.complete(id));}
}
