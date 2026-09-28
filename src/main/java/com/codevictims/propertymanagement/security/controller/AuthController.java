package com.codevictims.propertymanagement.security.controller;

import com.codevictims.propertymanagement.security.service.Access;

import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final Access access;

  public AuthController(Access access) {
    this.access = access;
  }

  @GetMapping("/csrf")
  Map<String, String> csrf(CsrfToken token) {
    return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
  }

  @GetMapping("/me")
  Map<String, Object> me() {
    var u = access.user();
    return Map.of(
        "id",
        u.id,
        "username",
        u.username,
        "name",
        u.displayName,
        "role",
        u.role,
        "buildings",
        access.buildings());
  }
}
