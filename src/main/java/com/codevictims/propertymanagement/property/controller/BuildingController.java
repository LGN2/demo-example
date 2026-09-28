package com.codevictims.propertymanagement.property.controller;
import com.codevictims.propertymanagement.property.mapper.BuildingMapper;
import com.codevictims.propertymanagement.property.dto.response.BuildingResponse;
import com.codevictims.propertymanagement.property.mapper.BuildingMapper;
import com.codevictims.propertymanagement.property.dto.response.BuildingResponse;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.property.dto.request.BuildingRequest;

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
    if (Set.of("OWNER", "MANAGER").contains(access.user().role)) return all.stream().map(BuildingMapper::toResponse).toList();
    return all.stream()
        .map(b -> Map.of("id", b.id, "name", b.name, "wilayat", b.wilayat, "address", b.address))
        .toList();
  }

  @PostMapping("/buildings")
  public BuildingResponse building(@jakarta.validation.Valid @RequestBody BuildingRequest b) {
    return BuildingMapper.toResponse(s.building(RequestMapper.toInput(b), null));
  }

  @PutMapping("/buildings/{id}")
  public BuildingResponse building(@PathVariable Long id, @jakarta.validation.Valid @RequestBody BuildingRequest b) {
    return BuildingMapper.toResponse(s.building(RequestMapper.toInput(b), id));
  }

}
