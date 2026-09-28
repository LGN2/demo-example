package com.codevictims.propertymanagement.property.controller;
import com.codevictims.propertymanagement.property.entity.Parking;
import com.codevictims.propertymanagement.property.dto.request.ParkingRequest;
import com.codevictims.propertymanagement.property.dto.response.ParkingResponse;
import com.codevictims.propertymanagement.property.mapper.ParkingMapper;
import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.property.service.OperationsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/operations/parking")
public class ParkingController {
  private final OperationsService service;
  private final ObjectMapper json;
  public ParkingController(OperationsService service, ObjectMapper json) { this.service=service; this.json=json; }
  @GetMapping
  public PageSlice<ParkingResponse> list(@RequestParam(required=false) Long buildingId,
      @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
      @RequestParam(defaultValue="") String q) {
    var rows=service.list("parking", buildingId).stream().map(Parking.class::cast).toList();
    return PageSlice.of(rows,page,size,q,r->{try {return json.writeValueAsString(r);}
      catch (com.fasterxml.jackson.core.JsonProcessingException e) {throw new IllegalStateException(e);}}).map(ParkingMapper::toResponse);
  }
  @GetMapping("/{id}")
  public ParkingResponse get(@PathVariable Long id) {return ParkingMapper.toResponse((Parking)service.get("parking",id));}
  @PostMapping
  public ParkingResponse create(@Valid @RequestBody ParkingRequest request) {return ParkingMapper.toResponse((Parking)service.save("parking",null,RequestMapper.toInput(request)));}
  @PutMapping("/{id}")
  public ParkingResponse update(@PathVariable Long id,@Valid @RequestBody ParkingRequest request) {return ParkingMapper.toResponse((Parking)service.save("parking",id,RequestMapper.toInput(request)));}
}
