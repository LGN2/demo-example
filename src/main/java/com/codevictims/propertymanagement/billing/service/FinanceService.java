package com.codevictims.propertymanagement.billing.service;

import com.codevictims.propertymanagement.billing.dto.response.DueBalanceResponse;
import com.codevictims.propertymanagement.billing.entity.Allocation;
import com.codevictims.propertymanagement.billing.entity.Cheque;
import com.codevictims.propertymanagement.billing.entity.DepositEntry;
import com.codevictims.propertymanagement.billing.entity.Due;
import com.codevictims.propertymanagement.billing.entity.Expense;
import com.codevictims.propertymanagement.billing.entity.FollowUp;
import com.codevictims.propertymanagement.billing.entity.Payment;
import com.codevictims.propertymanagement.billing.repository.AllocationRepository;
import com.codevictims.propertymanagement.billing.repository.ChequeRepository;
import com.codevictims.propertymanagement.billing.repository.DepositEntryRepository;
import com.codevictims.propertymanagement.billing.repository.DueRepository;
import com.codevictims.propertymanagement.billing.repository.ExpenseRepository;
import com.codevictims.propertymanagement.billing.repository.FollowUpRepository;
import com.codevictims.propertymanagement.billing.repository.PaymentRepository;
import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.exception.ApiException;
import com.codevictims.propertymanagement.common.repository.PersistenceSupport;
import com.codevictims.propertymanagement.common.service.AuditService;
import com.codevictims.propertymanagement.property.entity.Unit;
import com.codevictims.propertymanagement.security.service.Access;
import com.codevictims.propertymanagement.tenancy.entity.Lease;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class FinanceService {
  private final DueRepository dueRepository;
  private final AllocationRepository allocationRepository;
  private final PaymentRepository paymentRepository;
  private final ChequeRepository chequeRepository;
  private final DepositEntryRepository depositEntryRepository;
  private final FollowUpRepository followUpRepository;
  private final ExpenseRepository expenseRepository;
  private final PersistenceSupport db;
  private final Access access;
  private final AuditService audit;
  private final Clock clock;

  public FinanceService(
      PersistenceSupport db,
      Access access,
      AuditService audit,
      Clock clock,
      DueRepository dueRepository,
      AllocationRepository allocationRepository,
      PaymentRepository paymentRepository,
      ChequeRepository chequeRepository,
      DepositEntryRepository depositEntryRepository,
      FollowUpRepository followUpRepository,
      ExpenseRepository expenseRepository) {
    this.dueRepository = dueRepository;
    this.allocationRepository = allocationRepository;
    this.paymentRepository = paymentRepository;
    this.chequeRepository = chequeRepository;
    this.depositEntryRepository = depositEntryRepository;
    this.followUpRepository = followUpRepository;
    this.expenseRepository = expenseRepository;
    this.db = db;
    this.access = access;
    this.audit = audit;
    this.clock = clock;
  }

  public List<DueBalanceResponse> dues(Long leaseId, LocalDate asOf) {
    access.lease(leaseId, false);
    return dueRows(leaseId).stream().map(d -> view(d, asOf)).toList();
  }

  public List<Due> dueRows(Long leaseId) {
    return dueRepository.findByLeaseInAllocationOrder(leaseId);
  }

  public BigDecimal paid(Due due, LocalDate asOf) {
    return allocationRepository.findEffectiveForDue(due.id, asOf).stream()
        .map(a -> a.amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public DueBalanceResponse view(Due d, LocalDate asOf) {
    boolean cancelled = d.cancelled && (d.cancelledOn == null || !d.cancelledOn.isAfter(asOf));
    BigDecimal paid = paid(d, asOf),
        outstanding = cancelled ? BigDecimal.ZERO : d.amount.subtract(paid);
    long days =
        outstanding.signum() > 0 && d.dueDate.isBefore(asOf)
            ? java.time.temporal.ChronoUnit.DAYS.between(d.dueDate, asOf)
            : 0;
    return new DueBalanceResponse(
        d.id,
        d.leaseId,
        d.dueDate,
        d.rentAmount,
        d.taxAmount,
        d.amount,
        paid,
        outstanding,
        cancelled,
        days);
  }

  public List<Payment> payments(Long leaseId) {
    access.lease(leaseId, false);
    return paymentRepository.findByLeaseOrderByEffectiveDate(leaseId);
  }

  public Payment payment(Long leaseId, Input in) {
    access.lease(leaseId, true);
    Lease lease = db.lock(Lease.class, leaseId);
    return settle(
        lease,
        in.money("amount", true),
        in.choice("method", "BANK_TRANSFER", "CASH", "OTHER"),
        in.optional("reference"),
        in.date("effectiveDate"),
        in.text("idempotencyKey", 100));
  }

  private Payment settle(
      Lease l, BigDecimal amount, String method, String reference, LocalDate date, String key) {
    if (date.isAfter(LocalDate.now(clock))) throw ApiException.invalid("FUTURE_SETTLEMENT");
    var previous = paymentRepository.findByLeaseAndIdempotencyKey(l.id, key);
    if (!previous.isEmpty()) {
      Payment p = previous.get(0);
      if (p.amount.compareTo(amount) != 0
          || !p.method.equals(method)
          || !p.reference.equals(reference)
          || !p.effectiveDate.equals(date)) throw ApiException.conflict("IDEMPOTENCY_CONFLICT");
      return p;
    }
    for (Payment event : payments(l.id)) {
      LocalDate latest = event.reversedOn == null ? event.effectiveDate : event.reversedOn;
      if (date.isBefore(latest)) throw ApiException.invalid("BACKDATED_FINANCIAL_EVENT");
    }
    var balances =
        dueRows(l.id).stream()
            .filter(d -> !d.cancelled)
            .map(
                d -> new MoneyRules.Balance(d.id, d.amount.subtract(paid(d, LocalDate.now(clock)))))
            .toList();
    List<MoneyRules.Part> parts;
    try {
      parts = MoneyRules.allocate(amount, balances);
    } catch (IllegalArgumentException e) {
      throw ApiException.invalid(e.getMessage());
    }
    Payment p = new Payment();
    p.leaseId = l.id;
    p.amount = amount;
    p.method = method;
    p.reference = reference;
    p.effectiveDate = date;
    p.idempotencyKey = key;
    db.save(p);
    for (var part : parts) {
      Allocation a = new Allocation();
      a.paymentId = p.id;
      a.dueId = part.dueId();
      a.amount = part.amount();
      db.save(a);
    }
    audit.add(
        l.buildingId,
        "LEASE",
        l.id,
        "PAYMENT_SETTLED",
        "Payment #" + p.id + " " + amount.toPlainString() + " OMR");
    return p;
  }

  public Payment reverse(Long id, Input in) {
    Payment initial = db.get(Payment.class, id);
    Lease l = access.lease(initial.leaseId, true);
    db.lock(Lease.class, l.id);
    Payment p = db.lock(Payment.class, id);
    if (p.reversedOn != null) return p;
    p.reversedOn = LocalDate.now(clock);
    p.reversalReason = in.text("reason", 255);
    audit.add(
        l.buildingId,
        "LEASE",
        l.id,
        "PAYMENT_REVERSED",
        "Payment #" + id + ": " + p.reversalReason);
    return p;
  }

  public List<Cheque> cheques(Long leaseId) {
    access.lease(leaseId, false);
    return chequeRepository.findByLeaseOrderByChequeDate(leaseId);
  }

  public Cheque cheque(Long leaseId, Input in) {
    Lease l = access.lease(leaseId, true);
    Cheque c = new Cheque();
    c.leaseId = leaseId;
    c.chequeNumber = in.text("chequeNumber", 100);
    c.bank = in.text("bank", 255);
    c.chequeDate = in.date("chequeDate");
    c.amount = in.money("amount", true);
    c.status = "SCHEDULED";
    db.save(c);
    audit.add(l.buildingId, "LEASE", l.id, "CHEQUE_SCHEDULED", "Cheque #" + c.id);
    return c;
  }

  public Cheque transitionCheque(Long id, Input in) {
    Cheque initial = db.get(Cheque.class, id);
    Lease l = access.lease(initial.leaseId, true);
    db.lock(Lease.class, l.id);
    Cheque c = db.lock(Cheque.class, id);
    String next = in.choice("status", "DEPOSITED", "CLEARED", "BOUNCED", "CANCELLED");
    if (next.equals("CLEARED") && c.status.equals("CLEARED")) return c;
    boolean allowed =
        switch (c.status) {
          case "SCHEDULED" -> next.equals("DEPOSITED") || next.equals("CANCELLED");
          case "DEPOSITED" -> Set.of("CLEARED", "BOUNCED", "CANCELLED").contains(next);
          default -> false;
        };
    if (!allowed) throw ApiException.conflict("INVALID_TRANSITION");
    if (next.equals("CLEARED")) {
      LocalDate date = in.date("effectiveDate");
      if (date.isBefore(c.chequeDate)) throw ApiException.invalid("INVALID_DATE");
      Payment p = settle(l, c.amount, "CHEQUE", "Cheque #" + c.id, date, "cheque-" + c.id);
      c.paymentId = p.id;
    }
    c.status = next;
    audit.add(l.buildingId, "LEASE", l.id, "CHEQUE_" + next, "Cheque #" + id);
    return c;
  }

  public List<DepositEntry> deposits(Long leaseId) {
    access.lease(leaseId, false);
    return depositEntryRepository.findByLeaseInLedgerOrder(leaseId);
  }

  public BigDecimal depositEffect(DepositEntry e) {
    if (e.kind.equals("REVERSAL"))
      return depositEffect(db.get(DepositEntry.class, e.reversesId)).negate();
    return e.kind.equals("RECEIPT") ? e.amount : e.amount.negate();
  }

  public BigDecimal held(Long leaseId) {
    return deposits(leaseId).stream()
        .map(this::depositEffect)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public DepositEntry deposit(Long leaseId, Input in) {
    access.lease(leaseId, true);
    Lease l = db.lock(Lease.class, leaseId);
    String key = in.text("idempotencyKey", 100),
        kind = in.choice("kind", "RECEIPT", "DEDUCTION", "REFUND", "REVERSAL");
    BigDecimal amount = in.money("amount", true);
    LocalDate date = in.date("effectiveDate");
    String reason = in.text("reason", 255);
    Long originalId = in.nullableId("reversesId");
    if (date.isAfter(LocalDate.now(clock))) throw ApiException.invalid("FUTURE_SETTLEMENT");
    var entries = deposits(leaseId);
    for (var e : entries)
      if (e.idempotencyKey.equals(key)) {
        if (!e.kind.equals(kind)
            || e.amount.compareTo(amount) != 0
            || !e.effectiveDate.equals(date)
            || !e.reason.equals(reason)
            || !Objects.equals(e.reversesId, originalId))
          throw ApiException.conflict("IDEMPOTENCY_CONFLICT");
        return e;
      }
    if (entries.stream().anyMatch(entry -> date.isBefore(entry.effectiveDate)))
      throw ApiException.invalid("BACKDATED_FINANCIAL_EVENT");
    DepositEntry e = new DepositEntry();
    e.leaseId = leaseId;
    e.kind = kind;
    e.amount = amount;
    e.effectiveDate = date;
    e.reason = reason;
    e.idempotencyKey = key;
    if (kind.equals("REVERSAL")) {
      DepositEntry original = db.get(DepositEntry.class, originalId);
      if (!original.leaseId.equals(leaseId)
          || original.kind.equals("REVERSAL")
          || original.amount.compareTo(amount) != 0
          || date.isBefore(original.effectiveDate)
          || entries.stream().anyMatch(x -> Objects.equals(x.reversesId, original.id)))
        throw ApiException.invalid("INVALID_REVERSAL");
      e.reversesId = original.id;
    } else if (originalId != null) throw ApiException.invalid("INVALID_REVERSAL");
    BigDecimal balance = held(leaseId).add(depositEffect(e));
    if (balance.signum() < 0 || balance.compareTo(l.deposit) > 0)
      throw ApiException.invalid("INVALID_DEPOSIT_BALANCE");
    db.save(e);
    audit.add(
        l.buildingId, "LEASE", leaseId, "DEPOSIT_" + kind, "Deposit entry #" + e.id + " " + reason);
    return e;
  }

  public FollowUp followUp(Long leaseId, Input in) {
    Lease l = access.lease(leaseId, true);
    FollowUp f = new FollowUp();
    f.leaseId = leaseId;
    f.note = in.text("note", 255);
    f.nextDate = in.optionalDate("nextDate");
    db.save(f);
    audit.add(l.buildingId, "LEASE", l.id, "FOLLOW_UP", f.note);
    return f;
  }

  public List<FollowUp> followUps(Long leaseId) {
    Lease l = access.lease(leaseId, false);
    access.staffRead(l.buildingId);
    return followUpRepository.findByLeaseNewestFirst(leaseId);
  }

  public Expense expense(Input in) {
    Long bid = in.id("buildingId");
    access.manage(bid);
    Long unit = in.nullableId("unitId");
    if (unit != null && !db.get(Unit.class, unit).buildingId.equals(bid))
      throw ApiException.invalid("INVALID_INPUT");
    Expense e = new Expense();
    e.buildingId = bid;
    e.unitId = unit;
    e.amount = in.money("amount", true);
    e.expenseDate = in.date("expenseDate");
    if (e.expenseDate.isAfter(LocalDate.now(clock)))
      throw ApiException.invalid("FUTURE_SETTLEMENT");
    e.category = in.text("category", 40);
    e.description = in.text("description", 255);
    db.save(e);
    audit.add(bid, "EXPENSE", e.id, "RECORDED", e.description);
    return e;
  }

  public Expense reverseExpense(Long id, Input in) {
    Expense e = db.lock(Expense.class, id);
    access.manage(e.buildingId);
    if (e.reversedOn == null) {
      e.reversedOn = LocalDate.now(clock);
      e.reversalReason = in.text("reason", 255);
      audit.add(e.buildingId, "EXPENSE", id, "REVERSED", e.reversalReason);
    }
    return e;
  }

  public List<Expense> expenses(List<Long> ids) {
    access.role("OWNER", "MANAGER");
    for (Long id : ids) access.staffRead(id);
    return ids.isEmpty() ? List.of() : expenseRepository.findInBuildingsNewestFirst(ids);
  }
}
