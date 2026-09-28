package com.codevictims.propertymanagement.maintenance.repository;

import com.codevictims.propertymanagement.maintenance.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select m from Maintenance m,Tenant t where m.tenantId=t.id and t.accountId=:u and"
          + " m.buildingId in :ids order by m.id desc")
  java.util.List<Maintenance> findForTenantAccountInBuildings(
      @org.springframework.data.repository.query.Param("u") Long u,
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);

  @org.springframework.data.jpa.repository.Query(
      "select m from Maintenance m where m.assignedTo=:u and m.buildingId in :ids order by m.id"
          + " desc")
  java.util.List<Maintenance> findAssignedInBuildings(
      @org.springframework.data.repository.query.Param("u") Long u,
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);

  @org.springframework.data.jpa.repository.Query(
      "select m from Maintenance m where m.buildingId in :ids order by m.id desc")
  java.util.List<Maintenance> findInBuildings(
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);

  @org.springframework.data.jpa.repository.Query(
      "select m from Maintenance m where m.assignedTo=:u and m.buildingId=:b")
  java.util.List<Maintenance> findAssignedInBuilding(
      @org.springframework.data.repository.query.Param("u") Long u,
      @org.springframework.data.repository.query.Param("b") Long b);

  @org.springframework.data.jpa.repository.Query(
      "select m.id from Maintenance m where m.unitId=:unit and m.assignedTo=:user")
  java.util.List<Long> findAssignedIdsForUnit(
      @org.springframework.data.repository.query.Param("unit") Long unit,
      @org.springframework.data.repository.query.Param("user") Long user);
}
