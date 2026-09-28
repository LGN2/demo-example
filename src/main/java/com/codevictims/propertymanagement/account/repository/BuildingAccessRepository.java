package com.codevictims.propertymanagement.account.repository;

import com.codevictims.propertymanagement.account.entity.BuildingAccess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingAccessRepository extends JpaRepository<BuildingAccess, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select a from BuildingAccess a where a.buildingId=:b and a.userId=:u")
  java.util.List<BuildingAccess> findByBuildingAndUser(
      @org.springframework.data.repository.query.Param("b") Long b,
      @org.springframework.data.repository.query.Param("u") Long u);

  @org.springframework.data.jpa.repository.Query(
      "select a from BuildingAccess a where a.buildingId=:b")
  java.util.List<BuildingAccess> findByBuilding(
      @org.springframework.data.repository.query.Param("b") Long b);

  @org.springframework.data.jpa.repository.Query(
      "select a from BuildingAccess a where a.userId=:u and a.buildingId=:b and a.canWrite=true")
  java.util.List<BuildingAccess> findWritableByUserAndBuilding(
      @org.springframework.data.repository.query.Param("u") Long u,
      @org.springframework.data.repository.query.Param("b") Long b);

  @org.springframework.data.jpa.repository.Query(
      "select a.buildingId from BuildingAccess a where a.userId=:id")
  java.util.List<Long> findBuildingIdsForUser(
      @org.springframework.data.repository.query.Param("id") Long id);

  @org.springframework.data.jpa.repository.Query(
      "select a from BuildingAccess a where a.userId=:user and a.buildingId=:building and"
          + " a.canWrite=true")
  java.util.List<BuildingAccess> findWritableAssignment(
      @org.springframework.data.repository.query.Param("user") Long user,
      @org.springframework.data.repository.query.Param("building") Long building);
}
