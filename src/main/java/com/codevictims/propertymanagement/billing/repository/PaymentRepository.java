package com.codevictims.propertymanagement.billing.repository;

import com.codevictims.propertymanagement.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select p from Payment p where p.leaseId=:id order by p.effectiveDate desc,p.id desc")
  java.util.List<Payment> findByLeaseOrderByEffectiveDate(
      @org.springframework.data.repository.query.Param("id") Long id);

  @org.springframework.data.jpa.repository.Query(
      "select p from Payment p where p.leaseId=:id and p.idempotencyKey=:key")
  java.util.List<Payment> findByLeaseAndIdempotencyKey(
      @org.springframework.data.repository.query.Param("id") Long id,
      @org.springframework.data.repository.query.Param("key") String key);
}
