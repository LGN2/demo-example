package com.codevictims.propertymanagement.tenancy.controller;

import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.tenancy.service.TenancyService;
import com.codevictims.propertymanagement.security.service.Access;

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
  Object tenants(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    return PageSlice.of(
        s.tenants(buildingId), page, size, q, t -> t.name + " " + t.phone + " " + t.kind);
  }

  @PostMapping("/tenants")
  Object tenant(@RequestBody Map<String, Object> b) {
    return s.tenant(new Input(b), null);
  }

  @PutMapping("/tenants/{id}")
  Object tenant(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.tenant(new Input(b), id);
  }

}
