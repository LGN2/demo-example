package com.codevictims.propertymanagement.property.controller;
import com.codevictims.propertymanagement.property.mapper.MeterReadingMapper;
import com.codevictims.propertymanagement.property.dto.response.MeterReadingResponse;
import com.codevictims.propertymanagement.property.dto.request.MeterReadingRequest;
import com.codevictims.propertymanagement.property.entity.Meter;
import com.codevictims.propertymanagement.property.dto.request.MeterRequest;
import com.codevictims.propertymanagement.property.dto.response.MeterResponse;
import com.codevictims.propertymanagement.property.mapper.MeterMapper;
import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.property.service.OperationsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/operations/meters")
public class MeterController {
  private final OperationsService service;
  private final ObjectMapper json;
  public MeterController(OperationsService service, ObjectMapper json) { this.service=service; this.json=json; }
  @GetMapping
  public PageSlice<MeterResponse> list(@RequestParam(required=false) Long buildingId,
      @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
      @RequestParam(defaultValue="") String q) {
    var rows=service.list("meters", buildingId).stream().map(Meter.class::cast).toList();
    return PageSlice.of(rows,page,size,q,r->{try {return json.writeValueAsString(r);}
      catch (com.fasterxml.jackson.core.JsonProcessingException e) {throw new IllegalStateException(e);}}).map(MeterMapper::toResponse);
  }
  @GetMapping("/{id}")
  public MeterResponse get(@PathVariable Long id) {return MeterMapper.toResponse((Meter)service.get("meters",id));}
  @PostMapping
  public MeterResponse create(@Valid @RequestBody MeterRequest request) {return MeterMapper.toResponse((Meter)service.save("meters",null,RequestMapper.toInput(request)));}
  @PutMapping("/{id}")
  public MeterResponse update(@PathVariable Long id,@Valid @RequestBody MeterRequest request) {return MeterMapper.toResponse((Meter)service.save("meters",id,RequestMapper.toInput(request)));}
  @GetMapping("/{id}/readings")
  public java.util.List<MeterReadingResponse> readings(@PathVariable Long id) {return service.readings(id).stream().map(MeterReadingMapper::toResponse).toList();}
  @PostMapping("/{id}/readings")
  public MeterReadingResponse reading(@PathVariable Long id,@Valid @RequestBody MeterReadingRequest request) {return MeterReadingMapper.toResponse(service.reading(id,RequestMapper.toInput(request)));}
}
