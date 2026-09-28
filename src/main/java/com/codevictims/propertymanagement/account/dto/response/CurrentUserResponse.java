package com.codevictims.propertymanagement.account.dto.response;

public record CurrentUserResponse(
    Long id, String username, String name, String role, java.util.List<Long> buildings) {}
