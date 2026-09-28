package com.codevictims.propertymanagement.common.repository;

import com.codevictims.propertymanagement.common.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select a from AuditEvent a where a.resourceType=:type and a.resourceId=:id order by"
          + " a.createdAt")
  java.util.List<AuditEvent> findHistoryForResource(
      @org.springframework.data.repository.query.Param("type") String type,
      @org.springframework.data.repository.query.Param("id") Long id);
}
