package com.codevictims.propertymanagement.tenancy.repository;

import com.codevictims.propertymanagement.tenancy.entity.Lease;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface LeaseRepository extends JpaRepository<Lease, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select l from Lease l where l.id = :id")
  Optional<Lease> lockById(Long id);

  @org.springframework.data.jpa.repository.Query(
      "select l.id from Lease l where l.tenantId=:t and l.unitId=:u")
  java.util.List<Long> findIdsByTenantAndUnit(
      @org.springframework.data.repository.query.Param("t") Long t,
      @org.springframework.data.repository.query.Param("u") Long u);

  @org.springframework.data.jpa.repository.Query(
      "select l.id from Lease l, Tenant t where l.tenantId=t.id and l.unitId=:unit and"
          + " t.accountId=:user")
  java.util.List<Long> findIdsForTenantAccountAndUnit(
      @org.springframework.data.repository.query.Param("unit") Long unit,
      @org.springframework.data.repository.query.Param("user") Long user);

  @org.springframework.data.jpa.repository.Query(
      "select l from Lease l,Tenant t where l.tenantId=t.id and t.accountId=:id and l.buildingId in"
          + " :ids order by l.startDate desc")
  java.util.List<Lease> findForTenantAccountInBuildings(
      @org.springframework.data.repository.query.Param("id") Long id,
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);

  @org.springframework.data.jpa.repository.Query(
      "select l from Lease l where l.buildingId in :ids order by l.startDate desc")
  java.util.List<Lease> findInBuildings(
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);

  @org.springframework.data.jpa.repository.Query(
      "select l from Lease l where l.unitId=:id and l.startDate<=:end and l.endDate>=:start")
  java.util.List<Lease> findOverlappingTerms(
      @org.springframework.data.repository.query.Param("id") Long id,
      @org.springframework.data.repository.query.Param("start") LocalDate start,
      @org.springframework.data.repository.query.Param("end") LocalDate end);
}
