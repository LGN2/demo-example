package com.codevictims.propertymanagement.property.dto.response;

public record OperationalUnitResponse(
    Long id, Long buildingId, String code, String floorName, String kind, String availability) {}
