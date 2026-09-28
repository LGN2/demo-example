package com.codevictims.propertymanagement.billing.repository;

import com.codevictims.propertymanagement.billing.entity.TaxPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxPolicyRepository extends JpaRepository<TaxPolicy, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select p from TaxPolicy p where p.buildingId in :ids order by p.effectiveFrom")
  java.util.List<TaxPolicy> findInBuildingsByEffectiveDate(
      @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);
}
