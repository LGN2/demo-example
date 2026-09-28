package com.codevictims.propertymanagement.common.repository;

import com.codevictims.propertymanagement.common.entity.BaseEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class RepositoryCatalog {
  private final Map<Class<?>, JpaRepository<?, Long>> repositories = new HashMap<>();

  public RepositoryCatalog(
      com.codevictims.propertymanagement.billing.repository.ChequeRepository cheque,
      com.codevictims.propertymanagement.billing.repository.PaymentRepository payment,
      com.codevictims.propertymanagement.billing.repository.DueRepository due,
      com.codevictims.propertymanagement.billing.repository.DepositEntryRepository depositEntry,
      com.codevictims.propertymanagement.billing.repository.TaxPolicyRepository taxPolicy,
      com.codevictims.propertymanagement.billing.repository.ExpenseRepository expense,
      com.codevictims.propertymanagement.billing.repository.AllocationRepository allocation,
      com.codevictims.propertymanagement.billing.repository.FollowUpRepository followUp,
      com.codevictims.propertymanagement.common.repository.AuditEventRepository auditEvent,
      com.codevictims.propertymanagement.common.repository.DocumentRepository document,
      com.codevictims.propertymanagement.account.repository.UserRepository userAccount,
      com.codevictims.propertymanagement.account.repository.BuildingAccessRepository buildingAccess,
      com.codevictims.propertymanagement.maintenance.repository.PreventiveTaskRepository
          preventiveTask,
      com.codevictims.propertymanagement.maintenance.repository.VendorProfileRepository
          vendorProfile,
      com.codevictims.propertymanagement.maintenance.repository.MaintenanceRepository maintenance,
      com.codevictims.propertymanagement.property.repository.MeterReadingRepository meterReading,
      com.codevictims.propertymanagement.property.repository.NoticeRepository notice,
      com.codevictims.propertymanagement.property.repository.ParkingRepository parking,
      com.codevictims.propertymanagement.property.repository.UnitRepository unit,
      com.codevictims.propertymanagement.property.repository.BuildingRepository building,
      com.codevictims.propertymanagement.property.repository.MeterRepository meter,
      com.codevictims.propertymanagement.property.repository.GuardCheckInRepository guardCheckIn,
      com.codevictims.propertymanagement.property.repository.SafetyRecordRepository safetyRecord,
      com.codevictims.propertymanagement.property.repository.VisitRepository visit,
      com.codevictims.propertymanagement.tenancy.repository.LeadRepository lead,
      com.codevictims.propertymanagement.tenancy.repository.TenantRepository tenant,
      com.codevictims.propertymanagement.tenancy.repository.LeaseRepository lease) {
    repositories.put(com.codevictims.propertymanagement.billing.entity.Cheque.class, cheque);
    repositories.put(com.codevictims.propertymanagement.billing.entity.Payment.class, payment);
    repositories.put(com.codevictims.propertymanagement.billing.entity.Due.class, due);
    repositories.put(
        com.codevictims.propertymanagement.billing.entity.DepositEntry.class, depositEntry);
    repositories.put(com.codevictims.propertymanagement.billing.entity.TaxPolicy.class, taxPolicy);
    repositories.put(com.codevictims.propertymanagement.billing.entity.Expense.class, expense);
    repositories.put(
        com.codevictims.propertymanagement.billing.entity.Allocation.class, allocation);
    repositories.put(com.codevictims.propertymanagement.billing.entity.FollowUp.class, followUp);
    repositories.put(com.codevictims.propertymanagement.common.entity.AuditEvent.class, auditEvent);
    repositories.put(com.codevictims.propertymanagement.common.entity.Document.class, document);
    repositories.put(
        com.codevictims.propertymanagement.account.entity.UserAccount.class, userAccount);
    repositories.put(
        com.codevictims.propertymanagement.account.entity.BuildingAccess.class, buildingAccess);
    repositories.put(
        com.codevictims.propertymanagement.maintenance.entity.PreventiveTask.class, preventiveTask);
    repositories.put(
        com.codevictims.propertymanagement.maintenance.entity.VendorProfile.class, vendorProfile);
    repositories.put(
        com.codevictims.propertymanagement.maintenance.entity.Maintenance.class, maintenance);
    repositories.put(
        com.codevictims.propertymanagement.property.entity.MeterReading.class, meterReading);
    repositories.put(com.codevictims.propertymanagement.property.entity.Notice.class, notice);
    repositories.put(com.codevictims.propertymanagement.property.entity.Parking.class, parking);
    repositories.put(com.codevictims.propertymanagement.property.entity.Unit.class, unit);
    repositories.put(com.codevictims.propertymanagement.property.entity.Building.class, building);
    repositories.put(com.codevictims.propertymanagement.property.entity.Meter.class, meter);
    repositories.put(
        com.codevictims.propertymanagement.property.entity.GuardCheckIn.class, guardCheckIn);
    repositories.put(
        com.codevictims.propertymanagement.property.entity.SafetyRecord.class, safetyRecord);
    repositories.put(com.codevictims.propertymanagement.property.entity.Visit.class, visit);
    repositories.put(com.codevictims.propertymanagement.tenancy.entity.Lead.class, lead);
    repositories.put(com.codevictims.propertymanagement.tenancy.entity.Tenant.class, tenant);
    repositories.put(com.codevictims.propertymanagement.tenancy.entity.Lease.class, lease);
  }

  @SuppressWarnings("unchecked")
  public <T extends BaseEntity> JpaRepository<T, Long> repository(Class<T> type) {
    JpaRepository<?, Long> repository = repositories.get(type);
    if (repository == null)
      throw new IllegalArgumentException("No repository for " + type.getName());
    return (JpaRepository<T, Long>) repository;
  }
}
