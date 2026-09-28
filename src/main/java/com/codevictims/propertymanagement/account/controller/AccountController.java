package com.codevictims.propertymanagement.account.controller;

import com.codevictims.propertymanagement.account.dto.request.BuildingAccessRequest;
import com.codevictims.propertymanagement.account.dto.request.ChangePasswordRequest;
import com.codevictims.propertymanagement.account.dto.request.CreateUserRequest;
import com.codevictims.propertymanagement.account.dto.request.OwnerTaxStatusRequest;
import com.codevictims.propertymanagement.account.dto.response.BuildingAccessResponse;
import com.codevictims.propertymanagement.account.dto.response.UserAccountResponse;
import com.codevictims.propertymanagement.account.mapper.BuildingAccessMapper;
import com.codevictims.propertymanagement.account.mapper.UserAccountMapper;
import com.codevictims.propertymanagement.account.service.AccountService;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AccountController {
  private final AccountService service;

  public AccountController(AccountService service) {
    this.service = service;
  }

  @GetMapping("/users")
  public List<UserAccountResponse> list() {
    return service.list().stream().map(UserAccountMapper::toResponse).toList();
  }

  @PostMapping("/users")
  public UserAccountResponse create(@jakarta.validation.Valid @RequestBody CreateUserRequest b) {
    return UserAccountMapper.toResponse(service.create(RequestMapper.toInput(b)));
  }

  @GetMapping("/access")
  public List<BuildingAccessResponse> grants(@RequestParam Long buildingId) {
    return service.grants(buildingId).stream().map(BuildingAccessMapper::toResponse).toList();
  }

  @PostMapping("/access")
  public BuildingAccessResponse grant(
      @jakarta.validation.Valid @RequestBody BuildingAccessRequest b) {
    return BuildingAccessMapper.toResponse(service.assign(RequestMapper.toInput(b)));
  }

  @DeleteMapping("/access/{id}")
  void revoke(@PathVariable Long id) {
    service.revoke(id);
  }

  @PostMapping("/auth/password")
  void password(@jakarta.validation.Valid @RequestBody ChangePasswordRequest b) {
    service.password(RequestMapper.toInput(b));
  }

  @PutMapping("/owner/tax-status")
  public UserAccountResponse tax(@jakarta.validation.Valid @RequestBody OwnerTaxStatusRequest b) {
    return UserAccountMapper.toResponse(service.tax(RequestMapper.toInput(b)));
  }
}
