package com.codevictims.propertymanagement.billing.controller;

import com.codevictims.propertymanagement.billing.dto.request.TaxPolicyRequest;
import com.codevictims.propertymanagement.billing.dto.response.TaxPolicyResponse;
import com.codevictims.propertymanagement.billing.mapper.TaxPolicyMapper;
import com.codevictims.propertymanagement.billing.service.TaxPolicyService;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
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
  public List<TaxPolicyResponse> taxes(@RequestParam(required = false) Long buildingId) {
    return s.taxes(buildingId).stream().map(TaxPolicyMapper::toResponse).toList();
  }

  @PostMapping("/tax-policies")
  public TaxPolicyResponse tax(@jakarta.validation.Valid @RequestBody TaxPolicyRequest b) {
    return TaxPolicyMapper.toResponse(s.tax(RequestMapper.toInput(b)));
  }
}
