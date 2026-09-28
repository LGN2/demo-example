package com.codevictims.propertymanagement.billing.controller;

import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.billing.service.TaxPolicyService;
import com.codevictims.propertymanagement.security.service.Access;

import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TaxPolicyController {
  private final TaxPolicyService s;
  private final Access access;

  public TaxPolicyController(TaxPolicyService s, Access access) {
    this.s = s;
    this.access = access;
  }

  @GetMapping("/tax-policies")
  Object taxes(@RequestParam(required = false) Long buildingId) {
    return s.taxes(buildingId);
  }

  @PostMapping("/tax-policies")
  Object tax(@RequestBody Map<String, Object> b) {
    return s.tax(new Input(b));
  }

}
