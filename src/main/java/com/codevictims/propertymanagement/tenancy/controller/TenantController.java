package com.codevictims.propertymanagement.tenancy.controller;

import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.security.service.Access;
import com.codevictims.propertymanagement.tenancy.dto.request.TenantRequest;
import com.codevictims.propertymanagement.tenancy.dto.response.TenantResponse;
import com.codevictims.propertymanagement.tenancy.mapper.TenantMapper;
import com.codevictims.propertymanagement.tenancy.service.TenancyService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TenantController {
  private final TenancyService s;
  private final Access access;

  public TenantController(TenancyService s, Access access) {
    this.s = s;
    this.access = access;
  }

  @GetMapping("/tenants")
  public PageSlice<TenantResponse> tenants(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    return PageSlice.of(
            s.tenants(buildingId), page, size, q, t -> t.name + " " + t.phone + " " + t.kind)
        .map(TenantMapper::toResponse);
  }

  @PostMapping("/tenants")
  public TenantResponse tenant(@jakarta.validation.Valid @RequestBody TenantRequest b) {
    return TenantMapper.toResponse(s.tenant(RequestMapper.toInput(b), null));
  }

  @PutMapping("/tenants/{id}")
  public TenantResponse tenant(
      @PathVariable Long id, @jakarta.validation.Valid @RequestBody TenantRequest b) {
    return TenantMapper.toResponse(s.tenant(RequestMapper.toInput(b), id));
  }
}
