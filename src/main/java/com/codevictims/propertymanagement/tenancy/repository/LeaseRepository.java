package com.codevictims.propertymanagement.tenancy.repository;

import com.codevictims.propertymanagement.tenancy.entity.Lease;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface LeaseRepository extends JpaRepository<Lease, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select l from Lease l where l.id = :id")
  Optional<Lease> lockById(Long id);
}
