package com.codevictims.propertymanagement.common.repository;

import com.codevictims.propertymanagement.common.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select d from Document d where d.buildingId in :ids order by d.id desc")
  java.util.List<Document> findInBuildingsNewestFirst(
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);
}
