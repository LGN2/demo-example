package com.codevictims.propertymanagement.common.controller;

import com.codevictims.propertymanagement.common.dto.response.AuditEventResponse;
import com.codevictims.propertymanagement.common.mapper.AuditEventMapper;
import com.codevictims.propertymanagement.common.service.AuditHistoryService;
import com.codevictims.propertymanagement.security.service.Access;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuditHistoryController {
  private final AuditHistoryService s;
  private final Access access;

  public AuditHistoryController(AuditHistoryService s, Access access) {
    this.s = s;
    this.access = access;
  }

  @GetMapping("/history/{type}/{id}")
  public List<AuditEventResponse> history(@PathVariable String type, @PathVariable Long id) {
    return s.history(type, id).stream().map(AuditEventMapper::toResponse).toList();
  }
}
