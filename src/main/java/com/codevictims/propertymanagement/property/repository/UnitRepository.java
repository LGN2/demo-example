package com.codevictims.propertymanagement.property.repository;

import com.codevictims.propertymanagement.property.entity.Unit;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface UnitRepository extends JpaRepository<Unit, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from Unit u where u.id = :id")
  Optional<Unit> lockById(Long id);

  @org.springframework.data.jpa.repository.Query(
      "select distinct u from Unit u, Lease l, Tenant t where u.id=l.unitId and l.tenantId=t.id and"
          + " t.accountId=:user and u.buildingId in :ids order by u.id")
  java.util.List<Unit> findAccessibleToTenant(
      @org.springframework.data.repository.query.Param("user") Long user,
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);

  @org.springframework.data.jpa.repository.Query(
      "select distinct u from Unit u, Maintenance m where m.unitId=u.id and m.assignedTo=:user and"
          + " u.buildingId in :ids order by u.id")
  java.util.List<Unit> findAccessibleToVendor(
      @org.springframework.data.repository.query.Param("user") Long user,
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);

  @org.springframework.data.jpa.repository.Query(
      "select u from Unit u where u.buildingId in :ids order by u.id")
  java.util.List<Unit> findInBuildings(
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);
}
