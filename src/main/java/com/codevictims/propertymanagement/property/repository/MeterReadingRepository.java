package com.codevictims.propertymanagement.property.repository;

import com.codevictims.propertymanagement.property.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select r from MeterReading r where r.meterId=:id order by r.readingDate desc,r.id desc")
  java.util.List<MeterReading> findByMeterNewestFirst(
      @org.springframework.data.repository.query.Param("id") Long id);
}
