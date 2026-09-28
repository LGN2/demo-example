package com.codevictims.propertymanagement.common.mapper;

import com.codevictims.propertymanagement.common.entity.BaseEntity;
import java.util.*;

public final class AggregateResponseMapper {
  private AggregateResponseMapper() {}

  public static Object map(Object value) {
    if (value instanceof com.codevictims.propertymanagement.billing.entity.Cheque entity)
      return com.codevictims.propertymanagement.billing.mapper.ChequeMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.billing.entity.Payment entity)
      return com.codevictims.propertymanagement.billing.mapper.PaymentMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.billing.entity.Due entity)
      return com.codevictims.propertymanagement.billing.mapper.DueMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.billing.entity.DepositEntry entity)
      return com.codevictims.propertymanagement.billing.mapper.DepositEntryMapper.toResponse(
          entity);
    if (value instanceof com.codevictims.propertymanagement.billing.entity.TaxPolicy entity)
      return com.codevictims.propertymanagement.billing.mapper.TaxPolicyMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.billing.entity.Expense entity)
      return com.codevictims.propertymanagement.billing.mapper.ExpenseMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.billing.entity.Allocation entity)
      return com.codevictims.propertymanagement.billing.mapper.AllocationMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.billing.entity.FollowUp entity)
      return com.codevictims.propertymanagement.billing.mapper.FollowUpMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.common.entity.AuditEvent entity)
      return com.codevictims.propertymanagement.common.mapper.AuditEventMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.common.entity.Document entity)
      return com.codevictims.propertymanagement.common.mapper.DocumentMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.account.entity.UserAccount entity)
      return com.codevictims.propertymanagement.account.mapper.UserAccountMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.account.entity.BuildingAccess entity)
      return com.codevictims.propertymanagement.account.mapper.BuildingAccessMapper.toResponse(
          entity);
    if (value
        instanceof com.codevictims.propertymanagement.maintenance.entity.PreventiveTask entity)
      return com.codevictims.propertymanagement.maintenance.mapper.PreventiveTaskMapper.toResponse(
          entity);
    if (value instanceof com.codevictims.propertymanagement.maintenance.entity.VendorProfile entity)
      return com.codevictims.propertymanagement.maintenance.mapper.VendorProfileMapper.toResponse(
          entity);
    if (value instanceof com.codevictims.propertymanagement.maintenance.entity.Maintenance entity)
      return com.codevictims.propertymanagement.maintenance.mapper.MaintenanceMapper.toResponse(
          entity);
    if (value instanceof com.codevictims.propertymanagement.property.entity.MeterReading entity)
      return com.codevictims.propertymanagement.property.mapper.MeterReadingMapper.toResponse(
          entity);
    if (value instanceof com.codevictims.propertymanagement.property.entity.Notice entity)
      return com.codevictims.propertymanagement.property.mapper.NoticeMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.property.entity.Parking entity)
      return com.codevictims.propertymanagement.property.mapper.ParkingMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.property.entity.Unit entity)
      return com.codevictims.propertymanagement.property.mapper.UnitMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.property.entity.Building entity)
      return com.codevictims.propertymanagement.property.mapper.BuildingMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.property.entity.Meter entity)
      return com.codevictims.propertymanagement.property.mapper.MeterMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.property.entity.GuardCheckIn entity)
      return com.codevictims.propertymanagement.property.mapper.GuardCheckInMapper.toResponse(
          entity);
    if (value instanceof com.codevictims.propertymanagement.property.entity.SafetyRecord entity)
      return com.codevictims.propertymanagement.property.mapper.SafetyRecordMapper.toResponse(
          entity);
    if (value instanceof com.codevictims.propertymanagement.property.entity.Visit entity)
      return com.codevictims.propertymanagement.property.mapper.VisitMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.tenancy.entity.Lead entity)
      return com.codevictims.propertymanagement.tenancy.mapper.LeadMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.tenancy.entity.Tenant entity)
      return com.codevictims.propertymanagement.tenancy.mapper.TenantMapper.toResponse(entity);
    if (value instanceof com.codevictims.propertymanagement.tenancy.entity.Lease entity)
      return com.codevictims.propertymanagement.tenancy.mapper.LeaseMapper.toResponse(entity);
    if (value instanceof Map<?, ?> values) {
      Map<String, Object> result = new LinkedHashMap<>();
      values.forEach((k, v) -> result.put(k.toString(), map(v)));
      return result;
    }
    if (value instanceof List<?> values)
      return values.stream().map(AggregateResponseMapper::map).toList();
    if (value instanceof BaseEntity) throw new IllegalArgumentException("Missing response mapping");
    return value;
  }
}
