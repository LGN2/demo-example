package com.codevictims.propertymanagement.tenancy.repository;

import com.codevictims.propertymanagement.tenancy.entity.Tenant;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select t from Tenant t where t.accountId=:u and t.buildingId=:b")
  java.util.List<Tenant> findByAccountAndBuilding(
      @org.springframework.data.repository.query.Param("u") Long u,
      @org.springframework.data.repository.query.Param("b") Long b);

  @org.springframework.data.jpa.repository.Query(
      "select distinct t from Tenant t,Lease l where l.tenantId=t.id and l.unitId=:unit and"
          + " t.accountId=:user and l.startDate<=:today and l.endDate>=:today and (l.terminatedOn"
          + " is null or l.terminatedOn>=:today)")
  java.util.List<Tenant> findCurrentTenantForUnit(
      @org.springframework.data.repository.query.Param("unit") Long unit,
      @org.springframework.data.repository.query.Param("user") Long user,
      @org.springframework.data.repository.query.Param("today") LocalDate today);

  @org.springframework.data.jpa.repository.Query(
      "select distinct t.buildingId from Tenant t where t.accountId=:id")
  java.util.List<Long> findBuildingIdsForAccount(
      @org.springframework.data.repository.query.Param("id") Long id);

  @org.springframework.data.jpa.repository.Query(
      "select t from Tenant t where t.accountId=:id and t.buildingId in :ids order by t.id")
  java.util.List<Tenant> findForAccountInBuildings(
      @org.springframework.data.repository.query.Param("id") Long id,
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);

  @org.springframework.data.jpa.repository.Query(
      "select t from Tenant t where t.buildingId in :ids order by t.id")
  java.util.List<Tenant> findInBuildings(
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);
}
