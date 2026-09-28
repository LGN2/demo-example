package com.codevictims.propertymanagement.billing.repository;

import com.codevictims.propertymanagement.billing.entity.FollowUp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select f from FollowUp f where f.leaseId=:id order by f.createdAt desc")
  java.util.List<FollowUp> findByLeaseNewestFirst(
      @org.springframework.data.repository.query.Param("id") Long id);
}
