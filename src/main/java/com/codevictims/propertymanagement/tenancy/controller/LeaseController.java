package com.codevictims.propertymanagement.tenancy.controller;

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
  Object leases(
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
            l.id + " " + l.unitId + " " + l.tenantId + " " + l.status + " " + l.municipalityStatus);
  }

  @GetMapping("/leases/{id}")
  Object lease(@PathVariable Long id) {
    return access.lease(id, false);
  }

  @PostMapping("/leases")
  Object lease(@RequestBody Map<String, Object> b) {
    return s.lease(new Input(b), null);
  }

  @PostMapping("/leases/{id}/renew")
  Object renew(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.lease(new Input(b), id);
  }

  @PostMapping("/leases/{id}/terminate")
  Object terminate(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.terminate(id, new Input(b));
  }

  @PutMapping("/leases/{id}/municipality")
  Object municipality(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.municipality(id, new Input(b));
  }

}
