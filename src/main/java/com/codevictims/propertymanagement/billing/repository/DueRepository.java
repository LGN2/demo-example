package com.codevictims.propertymanagement.billing.repository;

import com.codevictims.propertymanagement.billing.entity.Due;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DueRepository extends JpaRepository<Due, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select d from Due d where d.leaseId=:id order by d.dueDate,d.id")
  java.util.List<Due> findByLeaseInAllocationOrder(
      @org.springframework.data.repository.query.Param("id") Long id);

  @org.springframework.data.jpa.repository.Query(
      "select d from Due d where d.leaseId=:l and d.dueDate>:date")
  java.util.List<Due> findFutureDues(
      @org.springframework.data.repository.query.Param("l") Long l,
      @org.springframework.data.repository.query.Param("date") LocalDate date);
}
