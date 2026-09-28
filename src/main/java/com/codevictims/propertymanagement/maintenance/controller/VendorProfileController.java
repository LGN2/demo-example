package com.codevictims.propertymanagement.maintenance.controller;
import com.codevictims.propertymanagement.maintenance.entity.VendorProfile;
import com.codevictims.propertymanagement.maintenance.dto.request.VendorRequest;
import com.codevictims.propertymanagement.maintenance.dto.response.VendorProfileResponse;
import com.codevictims.propertymanagement.maintenance.mapper.VendorProfileMapper;
import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.property.service.OperationsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/operations/vendors")
public class VendorProfileController {
  private final OperationsService service;
  private final ObjectMapper json;
  public VendorProfileController(OperationsService service, ObjectMapper json) { this.service=service; this.json=json; }
  @GetMapping
  public PageSlice<VendorProfileResponse> list(@RequestParam(required=false) Long buildingId,
      @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
      @RequestParam(defaultValue="") String q) {
    var rows=service.list("vendors", buildingId).stream().map(VendorProfile.class::cast).toList();
    return PageSlice.of(rows,page,size,q,r->{try {return json.writeValueAsString(r);}
      catch (com.fasterxml.jackson.core.JsonProcessingException e) {throw new IllegalStateException(e);}}).map(VendorProfileMapper::toResponse);
  }
  @GetMapping("/{id}")
  public VendorProfileResponse get(@PathVariable Long id) {return VendorProfileMapper.toResponse((VendorProfile)service.get("vendors",id));}
  @PostMapping
  public VendorProfileResponse create(@Valid @RequestBody VendorRequest request) {return VendorProfileMapper.toResponse((VendorProfile)service.save("vendors",null,RequestMapper.toInput(request)));}
  @PutMapping("/{id}")
  public VendorProfileResponse update(@PathVariable Long id,@Valid @RequestBody VendorRequest request) {return VendorProfileMapper.toResponse((VendorProfile)service.save("vendors",id,RequestMapper.toInput(request)));}
}
