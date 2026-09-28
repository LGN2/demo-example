package com.codevictims.propertymanagement.property.controller;

import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.property.service.PropertyService;
import com.codevictims.propertymanagement.security.service.Access;

import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BuildingController {
  private final PropertyService s;
  private final Access access;

  public BuildingController(PropertyService s, Access access) {
    this.s = s;
    this.access = access;
  }

  @GetMapping("/buildings")
  Object buildings() {
    var all = s.buildings();
    if (Set.of("OWNER", "MANAGER").contains(access.user().role)) return all;
    return all.stream()
        .map(b -> Map.of("id", b.id, "name", b.name, "wilayat", b.wilayat, "address", b.address))
        .toList();
  }

  @PostMapping("/buildings")
  Object building(@RequestBody Map<String, Object> b) {
    return s.building(new Input(b), null);
  }

  @PutMapping("/buildings/{id}")
  Object building(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.building(new Input(b), id);
  }

}
