package com.codevictims.propertymanagement.account.controller;

import com.codevictims.propertymanagement.account.dto.response.CsrfResponse;
import com.codevictims.propertymanagement.account.dto.response.CurrentUserResponse;
import com.codevictims.propertymanagement.security.service.Access;
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
  public CsrfResponse csrf(CsrfToken token) {
    return new CsrfResponse(token.getHeaderName(), token.getToken());
  }

  @GetMapping("/me")
  public CurrentUserResponse me() {
    var u = access.user();
    return new CurrentUserResponse(u.id, u.username, u.displayName, u.role, access.buildings());
  }
}
