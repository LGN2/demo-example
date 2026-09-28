package com.codevictims.propertymanagement.maintenance.repository;

import com.codevictims.propertymanagement.maintenance.entity.VendorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorProfileRepository extends JpaRepository<VendorProfile, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select v from VendorProfile v where v.userId=:u and v.buildingId=:b")
  java.util.List<VendorProfile> findForMaintenanceTransition(
      @org.springframework.data.repository.query.Param("u") Long u,
      @org.springframework.data.repository.query.Param("b") Long b);

  @org.springframework.data.jpa.repository.Query(
      "select v.buildingId from VendorProfile v where v.userId=:id")
  java.util.List<Long> findBuildingIdsForUser(
      @org.springframework.data.repository.query.Param("id") Long id);
}
