package com.codevictims.propertymanagement.billing.controller;
import com.codevictims.propertymanagement.billing.dto.response.DepositLedgerResponse;
import com.codevictims.propertymanagement.billing.mapper.ExpenseMapper;
import com.codevictims.propertymanagement.billing.dto.response.ExpenseResponse;
import com.codevictims.propertymanagement.billing.mapper.ExpenseMapper;
import com.codevictims.propertymanagement.billing.dto.response.ExpenseResponse;
import com.codevictims.propertymanagement.billing.mapper.ExpenseMapper;
import com.codevictims.propertymanagement.billing.dto.response.ExpenseResponse;
import com.codevictims.propertymanagement.billing.mapper.FollowUpMapper;
import com.codevictims.propertymanagement.billing.dto.response.FollowUpResponse;
import com.codevictims.propertymanagement.billing.mapper.FollowUpMapper;
import com.codevictims.propertymanagement.billing.dto.response.FollowUpResponse;
import com.codevictims.propertymanagement.billing.mapper.DepositEntryMapper;
import com.codevictims.propertymanagement.billing.dto.response.DepositEntryResponse;
import com.codevictims.propertymanagement.billing.mapper.ChequeMapper;
import com.codevictims.propertymanagement.billing.dto.response.ChequeResponse;
import com.codevictims.propertymanagement.billing.mapper.ChequeMapper;
import com.codevictims.propertymanagement.billing.dto.response.ChequeResponse;
import com.codevictims.propertymanagement.billing.mapper.ChequeMapper;
import com.codevictims.propertymanagement.billing.dto.response.ChequeResponse;
import com.codevictims.propertymanagement.billing.mapper.PaymentMapper;
import com.codevictims.propertymanagement.billing.dto.response.PaymentResponse;
import com.codevictims.propertymanagement.billing.mapper.PaymentMapper;
import com.codevictims.propertymanagement.billing.dto.response.PaymentResponse;
import com.codevictims.propertymanagement.billing.mapper.PaymentMapper;
import com.codevictims.propertymanagement.billing.dto.response.PaymentResponse;
import com.codevictims.propertymanagement.common.mapper.RequestMapper;
import com.codevictims.propertymanagement.billing.dto.request.ChequeRequest;
import com.codevictims.propertymanagement.billing.dto.request.ChequeStatusRequest;
import com.codevictims.propertymanagement.billing.dto.request.DepositRequest;
import com.codevictims.propertymanagement.billing.dto.request.ExpenseRequest;
import com.codevictims.propertymanagement.billing.dto.request.FollowUpRequest;
import com.codevictims.propertymanagement.billing.dto.request.PaymentRequest;
import com.codevictims.propertymanagement.billing.dto.request.ReversalRequest;

import com.codevictims.propertymanagement.billing.service.FinanceService;
import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.dto.PageSlice;
import com.codevictims.propertymanagement.property.service.PropertyService;

import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FinanceController {
  private final FinanceService s;
  private final PropertyService properties;
  private final Clock clock;

  public FinanceController(FinanceService s, PropertyService properties, Clock clock) {
    this.s = s;
    this.properties = properties;
    this.clock = clock;
  }

  @GetMapping("/leases/{id}/dues")
  Object dues(@PathVariable Long id, @RequestParam(required = false) LocalDate asOf) {
    return s.dues(id, asOf == null ? LocalDate.now(clock) : asOf);
  }

  @GetMapping("/leases/{id}/payments")
  public List<PaymentResponse> payments(@PathVariable Long id) {
    return s.payments(id).stream().map(PaymentMapper::toResponse).toList();
  }

  @PostMapping("/leases/{id}/payments")
  public PaymentResponse payment(@PathVariable Long id, @jakarta.validation.Valid @RequestBody PaymentRequest b) {
    return PaymentMapper.toResponse(s.payment(id, RequestMapper.toInput(b)));
  }

  @PostMapping("/payments/{id}/reverse")
  public PaymentResponse reverse(@PathVariable Long id, @jakarta.validation.Valid @RequestBody ReversalRequest b) {
    return PaymentMapper.toResponse(s.reverse(id, RequestMapper.toInput(b)));
  }

  @GetMapping("/leases/{id}/cheques")
  public List<ChequeResponse> cheques(@PathVariable Long id) {
    return s.cheques(id).stream().map(ChequeMapper::toResponse).toList();
  }

  @PostMapping("/leases/{id}/cheques")
  public ChequeResponse cheque(@PathVariable Long id, @jakarta.validation.Valid @RequestBody ChequeRequest b) {
    return ChequeMapper.toResponse(s.cheque(id, RequestMapper.toInput(b)));
  }

  @PostMapping("/cheques/{id}/status")
  public ChequeResponse chequeStatus(@PathVariable Long id, @jakarta.validation.Valid @RequestBody ChequeStatusRequest b) {
    return ChequeMapper.toResponse(s.transitionCheque(id, RequestMapper.toInput(b)));
  }

  @GetMapping("/leases/{id}/deposits")
  public DepositLedgerResponse deposits(@PathVariable Long id) {
    return new DepositLedgerResponse(s.deposits(id).stream().map(DepositEntryMapper::toResponse).toList(), s.held(id));
  }

  @PostMapping("/leases/{id}/deposits")
  public DepositEntryResponse deposit(@PathVariable Long id, @jakarta.validation.Valid @RequestBody DepositRequest b) {
    return DepositEntryMapper.toResponse(s.deposit(id, RequestMapper.toInput(b)));
  }

  @GetMapping("/leases/{id}/follow-ups")
  public List<FollowUpResponse> followUps(@PathVariable Long id) {
    return s.followUps(id).stream().map(FollowUpMapper::toResponse).toList();
  }

  @PostMapping("/leases/{id}/follow-ups")
  public FollowUpResponse followUp(@PathVariable Long id, @jakarta.validation.Valid @RequestBody FollowUpRequest b) {
    return FollowUpMapper.toResponse(s.followUp(id, RequestMapper.toInput(b)));
  }

  @GetMapping("/expenses")
  public PageSlice<ExpenseResponse> expenses(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    return PageSlice.of(
        s.expenses(properties.scope(buildingId)),
        page,
        size,
        q,
        e -> e.category + " " + e.description).map(ExpenseMapper::toResponse);
  }

  @PostMapping("/expenses")
  public ExpenseResponse expense(@jakarta.validation.Valid @RequestBody ExpenseRequest b) {
    return ExpenseMapper.toResponse(s.expense(RequestMapper.toInput(b)));
  }

  @PostMapping("/expenses/{id}/reverse")
  public ExpenseResponse reverseExpense(@PathVariable Long id, @jakarta.validation.Valid @RequestBody ReversalRequest b) {
    return ExpenseMapper.toResponse(s.reverseExpense(id, RequestMapper.toInput(b)));
  }
}
