package com.codevictims.propertymanagement.dashboard.service;

import com.codevictims.propertymanagement.billing.entity.Allocation;
import com.codevictims.propertymanagement.billing.entity.Cheque;
import com.codevictims.propertymanagement.billing.entity.Due;
import com.codevictims.propertymanagement.billing.entity.Expense;
import com.codevictims.propertymanagement.billing.entity.Payment;
import com.codevictims.propertymanagement.billing.service.FinanceService;
import com.codevictims.propertymanagement.common.exception.ApiException;
import com.codevictims.propertymanagement.common.repository.Store;
import com.codevictims.propertymanagement.maintenance.service.MaintenanceService;
import com.codevictims.propertymanagement.property.entity.Building;
import com.codevictims.propertymanagement.property.service.PropertyService;
import com.codevictims.propertymanagement.security.service.Access;

import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {
  private final Store db;
  private final Access access;
  private final PropertyService properties;
  private final FinanceService finance;
  private final MaintenanceService maintenance;

  public ReportService(
      Store db,
      Access access,
      PropertyService properties,
      FinanceService finance,
      MaintenanceService maintenance) {
    this.db = db;
    this.access = access;
    this.properties = properties;
    this.finance = finance;
    this.maintenance = maintenance;
  }

  public Map<String, Object> dashboard(
      Long buildingId, Long unitId, LocalDate from, LocalDate to, LocalDate asOf) {
    access.role("OWNER", "MANAGER", "TENANT");
    if (to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > 3660)
      throw ApiException.invalid("INVALID_DATE");
    var units =
        properties.units(buildingId).stream()
            .filter(
                u ->
                    (unitId == null || u.id.equals(unitId))
                        && !u.createdAt
                            .atZone(ZoneId.of("Asia/Muscat"))
                            .toLocalDate()
                            .isAfter(asOf))
            .toList();
    if (unitId != null) access.unit(unitId);
    var unitIds = units.stream().map(u -> u.id).toList();
    var leases =
        properties.leases(buildingId).stream().filter(l -> unitIds.contains(l.unitId)).toList();
    List<FinanceService.DueView> dues = new ArrayList<>();
    List<Payment> payments = new ArrayList<>();
    List<Cheque> cheques = new ArrayList<>();
    for (var l : leases) {
      dues.addAll(finance.dues(l.id, asOf));
      payments.addAll(finance.payments(l.id));
      cheques.addAll(finance.cheques(l.id));
    }
    BigDecimal expected = BigDecimal.ZERO,
        expectedTax = BigDecimal.ZERO,
        outstanding = BigDecimal.ZERO,
        overdue = BigDecimal.ZERO,
        future = BigDecimal.ZERO,
        collections = BigDecimal.ZERO,
        rentalCollections = BigDecimal.ZERO;
    List<Map<String, Object>> collectionEvents = new ArrayList<>();
    Map<String, BigDecimal> aging = new LinkedHashMap<>();
    for (String k : List.of("1_30", "31_60", "61_90", "90_PLUS")) aging.put(k, BigDecimal.ZERO);
    for (var d : dues) {
      if (!d.cancelled() && !d.dueDate().isBefore(from) && !d.dueDate().isAfter(to)) {
        expected = expected.add(d.rentAmount());
        expectedTax = expectedTax.add(d.taxAmount());
      }
      if (d.dueDate().isAfter(asOf)) future = future.add(d.outstanding());
      else outstanding = outstanding.add(d.outstanding());
      if (d.daysLate() > 0) {
        overdue = overdue.add(d.outstanding());
        String band =
            d.daysLate() <= 30
                ? "1_30"
                : d.daysLate() <= 60 ? "31_60" : d.daysLate() <= 90 ? "61_90" : "90_PLUS";
        aging.put(band, aging.get(band).add(d.outstanding()));
      }
    }
    for (var p : payments) {
      if (inPeriod(p.effectiveDate, from, to))
        collectionEvents.add(
            Map.of(
                "id",
                p.id,
                "leaseId",
                p.leaseId,
                "effectiveDate",
                p.effectiveDate,
                "amount",
                p.amount,
                "kind",
                "RECEIPT"));
      if (p.reversedOn != null && inPeriod(p.reversedOn, from, to))
        collectionEvents.add(
            Map.of(
                "id",
                p.id,
                "leaseId",
                p.leaseId,
                "effectiveDate",
                p.reversedOn,
                "amount",
                p.amount.negate(),
                "kind",
                "REVERSAL"));
      int sign = 0;
      if (inPeriod(p.effectiveDate, from, to)) sign++;
      if (p.reversedOn != null && inPeriod(p.reversedOn, from, to)) sign--;
      if (sign != 0) {
        collections = collections.add(p.amount.multiply(BigDecimal.valueOf(sign)));
        for (var a :
            db.list(
                Allocation.class, "select a from Allocation a where a.paymentId=:p", "p", p.id)) {
          Due d = db.get(Due.class, a.dueId);
          rentalCollections =
              rentalCollections.add(
                  a.amount
                      .multiply(d.rentAmount)
                      .divide(d.amount, 3, RoundingMode.HALF_UP)
                      .multiply(BigDecimal.valueOf(sign)));
        }
      }
    }
    Set<Long> occupied = new HashSet<>();
    for (var l : leases)
      if (!asOf.isBefore(l.startDate)
          && !asOf.isAfter(l.endDate)
          && (l.terminatedOn == null || !asOf.isAfter(l.terminatedOn))) occupied.add(l.unitId);
    List<Map<String, Object>> vacancies = new ArrayList<>();
    for (var u : units)
      if (!occupied.contains(u.id)) {
        LocalDate since = u.createdAt.atZone(ZoneId.of("Asia/Muscat")).toLocalDate();
        for (var l : leases)
          if (l.unitId.equals(u.id)) {
            LocalDate end = l.terminatedOn == null ? l.endDate : l.terminatedOn;
            if (end.isBefore(asOf) && end.plusDays(1).isAfter(since)) since = end.plusDays(1);
          }
        long days = Math.max(0, ChronoUnit.DAYS.between(since, asOf));
        vacancies.add(
            Map.of(
                "unit",
                u,
                "since",
                since,
                "days",
                days,
                "estimatedForegoneRent",
                u.marketRent
                    .multiply(BigDecimal.valueOf(days))
                    .divide(BigDecimal.valueOf(30), 3, RoundingMode.HALF_UP)));
      }
    var expiries =
        leases.stream()
            .filter(
                l ->
                    l.terminatedOn == null
                        && !l.endDate.isBefore(asOf)
                        && !l.endDate.isAfter(
                            asOf.plusDays(db.get(Building.class, l.buildingId).reminderDays)))
            .toList();
    var jobs =
        maintenance.list(buildingId).stream()
            .filter(m -> unitIds.contains(m.unitId) && !m.status.equals("CLOSED"))
            .toList();
    boolean tenant = access.user().role.equals("TENANT");
    var expenseRows =
        tenant
            ? List.<Expense>of()
            : finance.expenses(properties.scope(buildingId)).stream()
                .filter(e -> unitId == null || Objects.equals(e.unitId, unitId))
                .toList();
    BigDecimal expenses = BigDecimal.ZERO;
    for (var e : expenseRows) {
      if (inPeriod(e.expenseDate, from, to)) expenses = expenses.add(e.amount);
      if (e.reversedOn != null && inPeriod(e.reversedOn, from, to))
        expenses = expenses.subtract(e.amount);
    }
    var result = new LinkedHashMap<String, Object>();
    result.put("collectionEvents", collectionEvents);
    result.put(
        "occupancy",
        units.stream()
            .map(
                u ->
                    Map.of(
                        "id",
                        u.id,
                        "buildingId",
                        u.buildingId,
                        "code",
                        u.code,
                        "occupied",
                        occupied.contains(u.id),
                        "availability",
                        u.availability))
            .toList());
    result.put("from", from);
    result.put("to", to);
    result.put("asOf", asOf);
    result.put("unitCount", units.size());
    result.put("occupied", occupied.size());
    result.put("vacant", vacancies.size());
    result.put(
        "occupancyPercent",
        units.isEmpty()
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(occupied.size() * 100L)
                .divide(BigDecimal.valueOf(units.size()), 1, RoundingMode.HALF_UP));
    result.put("expectedRent", expected);
    result.put("expectedTax", expectedTax);
    result.put("settledCollections", collections);
    result.put("rentalCollections", rentalCollections);
    result.put("outstanding", outstanding);
    result.put("overdue", overdue);
    result.put("futureUnpaid", future);
    result.put("recordedExpenses", expenses);
    result.put("netOperatingIncome", rentalCollections.subtract(expenses));
    result.put(
        "costPerUnit",
        units.isEmpty()
            ? BigDecimal.ZERO
            : expenses.divide(BigDecimal.valueOf(units.size()), 3, RoundingMode.HALF_UP));
    result.put(
        "pendingCheques",
        cheques.stream().filter(c -> Set.of("SCHEDULED", "DEPOSITED").contains(c.status)).count());
    result.put("bouncedCheques", cheques.stream().filter(c -> c.status.equals("BOUNCED")).count());
    result.put("openMaintenance", jobs.size());
    result.put("aging", aging);
    result.put("dues", dues);
    result.put("payments", payments);
    result.put("cheques", cheques);
    result.put("vacancies", vacancies);
    result.put("leaseExpiries", expiries);
    result.put("maintenance", jobs);
    result.put("expenses", expenseRows);
    // Yield has an explicit portfolio basis, is period-specific, and is never annualized
    // implicitly.
    var buildings =
        properties.buildings().stream()
            .filter(b -> buildingId == null || b.id.equals(buildingId))
            .toList();
    if (!tenant
        && unitId == null
        && !buildings.isEmpty()
        && buildings.stream()
            .allMatch(b -> b.investmentValue != null && b.investmentValue.signum() > 0)) {
      var basis =
          buildings.stream().map(b -> b.investmentValue).reduce(BigDecimal.ZERO, BigDecimal::add);
      result.put("investmentBasis", basis);
      result.put(
          "periodNetYieldPercent",
          rentalCollections
              .subtract(expenses)
              .multiply(BigDecimal.valueOf(100))
              .divide(basis, 3, RoundingMode.HALF_UP));
    }
    return result;
  }

  private boolean inPeriod(LocalDate d, LocalDate from, LocalDate to) {
    return !d.isBefore(from) && !d.isAfter(to);
  }

  public String csv(Long building, Long unit, LocalDate from, LocalDate to, LocalDate asOf) {
    access.role("OWNER", "MANAGER");
    Map<String, Object> report = dashboard(building, unit, from, to, asOf);
    StringBuilder out = new StringBuilder("\ufeffmetric,value\r\n");
    for (var e : report.entrySet())
      if (e.getValue() instanceof Number || e.getValue() instanceof LocalDate)
        out.append(cell(e.getKey()))
            .append(',')
            .append(cell(e.getValue().toString()))
            .append("\r\n");
    out.append("\r\ndue_id,lease_id,due_date,amount,paid,outstanding,days_late\r\n");
    @SuppressWarnings("unchecked")
    var dues = (List<FinanceService.DueView>) report.get("dues");
    for (var d : dues)
      out.append(d.id())
          .append(',')
          .append(d.leaseId())
          .append(',')
          .append(d.dueDate())
          .append(',')
          .append(d.amount())
          .append(',')
          .append(d.paid())
          .append(',')
          .append(d.outstanding())
          .append(',')
          .append(d.daysLate())
          .append("\r\n");
    return out.toString();
  }

  public static String cell(String value) {
    String safe = value.matches("^[=+@\\-].*") ? "'" + value : value;
    return "\"" + safe.replace("\"", "\"\"") + "\"";
  }
}
