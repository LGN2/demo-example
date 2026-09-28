package com.codevictims.propertymanagement.property.controller;

import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.property.service.PropertyService;
import com.codevictims.propertymanagement.security.service.Access;

import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UnitController {
  private final PropertyService s;
  private final Access access;

  public UnitController(PropertyService s, Access access) {
    this.s = s;
    this.access = access;
  }

  @GetMapping("/units")
  Object units(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    var result =
        PageSlice.of(
            s.units(buildingId),
            page,
            size,
            q,
            u -> u.code + " " + u.floorName + " " + u.kind + " " + u.availability);
    if (Set.of("GUARD", "VENDOR").contains(access.user().role))
      return new PageSlice<>(
          result.items().stream().map(this::operationalUnit).toList(),
          result.total(),
          result.page(),
          result.size());
    return result;
  }

  @GetMapping("/units/{id}")
  Object unit(@PathVariable Long id) {
    var u = access.unit(id);
    return Set.of("GUARD", "VENDOR").contains(access.user().role) ? operationalUnit(u) : u;
  }

  private Map<String, Object> operationalUnit(com.codevictims.propertymanagement.property.entity.Unit u) {
    return Map.of(
        "id",
        u.id,
        "buildingId",
        u.buildingId,
        "code",
        u.code,
        "floorName",
        u.floorName,
        "kind",
        u.kind,
        "availability",
        u.availability);
  }

  @PostMapping("/units")
  Object unit(@RequestBody Map<String, Object> b) {
    return s.unit(new Input(b), null);
  }

  @PutMapping("/units/{id}")
  Object unit(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.unit(new Input(b), id);
  }

}
