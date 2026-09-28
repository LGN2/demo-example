package com.codevictims.propertymanagement.billing.repository;

import com.codevictims.propertymanagement.billing.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select e from Expense e where e.buildingId in :ids order by e.expenseDate desc")
  java.util.List<Expense> findInBuildingsNewestFirst(
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);
}
