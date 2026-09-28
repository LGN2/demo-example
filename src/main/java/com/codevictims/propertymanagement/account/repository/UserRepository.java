package com.codevictims.propertymanagement.account.repository;

import com.codevictims.propertymanagement.account.entity.UserAccount;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByUsername(String username);
}
