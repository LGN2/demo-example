package com.codevictims.propertymanagement.account.repository;

import com.codevictims.propertymanagement.account.entity.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByUsername(String username);

  @org.springframework.data.jpa.repository.Query(
      "select u from UserAccount u where u.role in ('OWNER','PLATFORM_ADMIN') order by u.id")
  java.util.List<UserAccount> findPlatformAccounts();

  @org.springframework.data.jpa.repository.Query(
      "select u from UserAccount u where u.ownerId=:id order by u.id")
  java.util.List<UserAccount> findPortfolioAccounts(
      @org.springframework.data.repository.query.Param("id") Long id);

  @org.springframework.data.jpa.repository.Query(
      "select distinct u from UserAccount u where u.active=true and (u.id=:owner or u.id in (select"
          + " a.userId from BuildingAccess a where a.buildingId=:b and a.canWrite=true) or u.id in"
          + " (select v.userId from VendorProfile v where v.buildingId=:b))")
  java.util.List<UserAccount> findEligibleMaintenanceAssignees(
      @org.springframework.data.repository.query.Param("owner") Long owner,
      @org.springframework.data.repository.query.Param("b") Long b);
}
