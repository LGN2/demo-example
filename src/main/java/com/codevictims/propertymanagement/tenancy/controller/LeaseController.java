package com.codevictims.propertymanagement.tenancy.controller;
import com.codevictims.propertymanagement.tenancy.mapper.LeaseMapper;
import com.codevictims.propertymanagement.tenancy.dto.response.LeaseResponse;
import com.codevictims.propertymanagement.tenancy.mapper.LeaseMapper;
import com.codevictims.propertymanagement.tenancy.dto.response.LeaseResponse;
import com.codevictims.propertymanagement.tenancy.mapper.LeaseMapper;
import com.codevictims.propertymanagement.tenancy.dto.response.LeaseResponse;
import com.codevictims.propertymanagement.tenancy.mapper.LeaseMapper;
import com.codevictims.propertymanagement.tenancy.dto.response.LeaseResponse;
import com.codevictims.propertymanagement.tenancy.mapper.LeaseMapper;
import com.codevictims.propertymanagement.tenancy.dto.response.LeaseResponse;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.tenancy.dto.request.LeaseRequest;
import com.codevictims.propertymanagement.tenancy.dto.request.MunicipalityRequest;
import com.codevictims.propertymanagement.tenancy.dto.request.TerminateLeaseRequest;

import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.tenancy.service.TenancyService;
import com.codevictims.propertymanagement.security.service.Access;

import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LeaseController {
  private final TenancyService s;
  private final Access access;

  public LeaseController(TenancyService s, Access access) {
    this.s = s;
    this.access = access;
  }

  @GetMapping("/leases")
  public PageSlice<LeaseResponse> leases(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    return PageSlice.of(
        s.leases(buildingId),
        page,
        size,
        q,
        l ->
            l.id + " " + l.unitId + " " + l.tenantId + " " + l.status + " " + l.municipalityStatus).map(LeaseMapper::toResponse);
  }

  @GetMapping("/leases/{id}")
  public LeaseResponse lease(@PathVariable Long id) {
    return LeaseMapper.toResponse(access.lease(id, false));
  }

  @PostMapping("/leases")
  public LeaseResponse lease(@jakarta.validation.Valid @RequestBody LeaseRequest b) {
    return LeaseMapper.toResponse(s.lease(RequestMapper.toInput(b), null));
  }

  @PostMapping("/leases/{id}/renew")
  public LeaseResponse renew(@PathVariable Long id, @jakarta.validation.Valid @RequestBody LeaseRequest b) {
    return LeaseMapper.toResponse(s.lease(RequestMapper.toInput(b), id));
  }

  @PostMapping("/leases/{id}/terminate")
  public LeaseResponse terminate(@PathVariable Long id, @jakarta.validation.Valid @RequestBody TerminateLeaseRequest b) {
    return LeaseMapper.toResponse(s.terminate(id, RequestMapper.toInput(b)));
  }

  @PutMapping("/leases/{id}/municipality")
  public LeaseResponse municipality(@PathVariable Long id, @jakarta.validation.Valid @RequestBody MunicipalityRequest b) {
    return LeaseMapper.toResponse(s.municipality(id, RequestMapper.toInput(b)));
  }

}
