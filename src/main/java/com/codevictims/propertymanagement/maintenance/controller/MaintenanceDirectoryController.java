package com.codevictims.propertymanagement.maintenance.controller;
import com.codevictims.propertymanagement.property.service.OperationsService;

import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.property.service.OperationsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MaintenanceDirectoryController {
  private final OperationsService s;
  private final ObjectMapper json;

  public MaintenanceDirectoryController(OperationsService s, ObjectMapper json) {
    this.s = s;
    this.json = json;
  }

  @GetMapping("/vendor-performance")
  Object performance(@RequestParam(required = false) Long buildingId) {
    return s.vendorPerformance(buildingId);
  }

  @GetMapping("/assignees")
  Object assignees(@RequestParam Long buildingId) {
    return s.assignees(buildingId).stream()
        .map(u -> Map.of("id", u.id, "displayName", u.displayName, "role", u.role))
        .toList();
  }
}
