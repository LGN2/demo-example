package com.codevictims.propertymanagement.dashboard.service;
import com.codevictims.propertymanagement.tenancy.service.TenancyService;


import com.codevictims.propertymanagement.billing.service.FinanceService;
import com.codevictims.propertymanagement.common.repository.Store;
import com.codevictims.propertymanagement.common.service.FileVault;
import com.codevictims.propertymanagement.maintenance.entity.PreventiveTask;
import com.codevictims.propertymanagement.property.entity.Building;
import com.codevictims.propertymanagement.property.entity.SafetyRecord;
import com.codevictims.propertymanagement.property.service.OperationsService;
import com.codevictims.propertymanagement.property.service.PropertyService;
import com.codevictims.propertymanagement.security.service.Access;
import com.codevictims.propertymanagement.tenancy.entity.Lead;
import com.codevictims.propertymanagement.tenancy.entity.Lease;

import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReminderService {
  private final TenancyService tenancyService;
  public record Reminder(
      String type, Long id, Long buildingId, LocalDate dueDate, String title, String link) {}

  private final PropertyService properties;
  private final FinanceService finance;
  private final FileVault vault;
  private final OperationsService operations;
  private final Access access;
  private final Store db;
  private final Clock clock;

  public ReminderService(
      PropertyService properties,
      FinanceService finance,
      FileVault vault,
      OperationsService operations,
      Access access,
      Store db,
      Clock clock, TenancyService tenancyService) {
    this.tenancyService = tenancyService;
    this.properties = properties;
    this.finance = finance;
    this.vault = vault;
    this.operations = operations;
    this.access = access;
    this.db = db;
    this.clock = clock;
  }

  public List<Reminder> reminders(Long building) {
    access.role("OWNER", "MANAGER", "TENANT");
    LocalDate today = LocalDate.now(clock);
    List<Reminder> rows = new ArrayList<>();
    for (Lease l : tenancyService.leases(building)) {
      int days = db.get(Building.class, l.buildingId).reminderDays;
      if (l.terminatedOn == null && !l.endDate.isAfter(today.plusDays(days)))
        rows.add(
            new Reminder(
                "LEASE_EXPIRY", l.id, l.buildingId, l.endDate, "#" + l.id, "#lease/" + l.id));
      for (var c : finance.cheques(l.id))
        if (c.status.equals("SCHEDULED") && !c.chequeDate.isAfter(today.plusDays(7)))
          rows.add(
              new Reminder(
                  "CHEQUE_DEPOSIT",
                  c.id,
                  l.buildingId,
                  c.chequeDate,
                  c.chequeNumber,
                  "#lease/" + l.id));
      for (var d : finance.dues(l.id, today))
        if (d.daysLate() > 0)
          rows.add(
              new Reminder(
                  "OVERDUE_RENT", d.id(), l.buildingId, d.dueDate(), "#" + l.id, "#lease/" + l.id));
    }
    for (var d : vault.list(building))
      if (d.expiryDate != null && !d.expiryDate.isAfter(today.plusDays(30)))
        rows.add(
            new Reminder(
                "DOCUMENT_EXPIRY", d.id, d.buildingId, d.expiryDate, d.filename, "#documents"));
    if (Set.of("OWNER", "MANAGER").contains(access.user().role)) {
      for (var row : operations.list("safety", building)) {
        var s = (SafetyRecord) row;
        if (!s.expiryDate.isAfter(today.plusDays(30)))
          rows.add(
              new Reminder("SAFETY_EXPIRY", s.id, s.buildingId, s.expiryDate, s.kind, "#safety"));
      }
      for (var row : operations.list("preventive", building)) {
        var p = (PreventiveTask) row;
        if (p.enabled && !p.nextDue.isAfter(today.plusDays(7)))
          rows.add(
              new Reminder(
                  "PREVENTIVE_DUE", p.id, p.buildingId, p.nextDue, p.title, "#preventive"));
      }
      for (var row : operations.list("leads", building)) {
        var l = (Lead) row;
        if (l.followUpDate != null
            && !Set.of("WON", "LOST").contains(l.status)
            && !l.followUpDate.isAfter(today))
          rows.add(
              new Reminder("LEAD_FOLLOW_UP", l.id, l.buildingId, l.followUpDate, l.name, "#leads"));
      }
    }
    rows.sort(Comparator.comparing(Reminder::dueDate));
    return rows;
  }
}
