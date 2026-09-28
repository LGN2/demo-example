package com.codevictims.propertymanagement.property.controller;

import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.property.dto.request.UnitRequest;
import com.codevictims.propertymanagement.property.dto.response.OperationalUnitResponse;
import com.codevictims.propertymanagement.property.dto.response.UnitResponse;
import com.codevictims.propertymanagement.property.mapper.UnitMapper;
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
  public PageSlice<?> units(
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
    return result.map(UnitMapper::toResponse);
  }

  @GetMapping("/units/{id}")
  public Object unit(@PathVariable Long id) {
    var u = access.unit(id);
    return Set.of("GUARD", "VENDOR").contains(access.user().role)
        ? operationalUnit(u)
        : UnitMapper.toResponse(u);
  }

  private OperationalUnitResponse operationalUnit(
      com.codevictims.propertymanagement.property.entity.Unit u) {
    return new OperationalUnitResponse(
        u.id, u.buildingId, u.code, u.floorName, u.kind, u.availability);
  }

  @PostMapping("/units")
  public UnitResponse unit(@jakarta.validation.Valid @RequestBody UnitRequest b) {
    return UnitMapper.toResponse(s.unit(RequestMapper.toInput(b), null));
  }

  @PutMapping("/units/{id}")
  public UnitResponse unit(
      @PathVariable Long id, @jakarta.validation.Valid @RequestBody UnitRequest b) {
    return UnitMapper.toResponse(s.unit(RequestMapper.toInput(b), id));
  }
}
