package com.codevictims.propertymanagement.billing.mapper;
import com.codevictims.propertymanagement.billing.entity.Expense;
import com.codevictims.propertymanagement.billing.dto.response.ExpenseResponse;
public final class ExpenseMapper {
  private ExpenseMapper() {}
  public static ExpenseResponse toResponse(Expense entity) {
    if (entity == null) return null;
    return new ExpenseResponse(entity.id, entity.version, entity.createdAt, entity.buildingId, entity.unitId, entity.amount, entity.expenseDate, entity.category, entity.description, entity.reversedOn, entity.reversalReason);
  }
}
