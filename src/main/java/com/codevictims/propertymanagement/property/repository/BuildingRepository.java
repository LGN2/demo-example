package com.codevictims.propertymanagement.property.repository;

import com.codevictims.propertymanagement.property.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select b from Building b where b.id in :ids order by b.id")
  java.util.List<Building> findAccessibleBuildings(
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);

  @org.springframework.data.jpa.repository.Query("select b.id from Building b where b.ownerId=:id")
  java.util.List<Long> findIdsByOwner(
      @org.springframework.data.repository.query.Param("id") Long id);
}
