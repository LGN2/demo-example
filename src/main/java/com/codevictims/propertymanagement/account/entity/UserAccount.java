package com.codevictims.propertymanagement.account.entity;

import com.codevictims.propertymanagement.common.entity.Row;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "user_account")
public class UserAccount extends Row {
  @Column(nullable = false)
  public String username = "";

  @Column(nullable = false)
  @JsonIgnore
  public String passwordHash = "";

  @Column(nullable = false)
  public String displayName = "";

  @Column(nullable = false, length = 40)
  public String role = "";

  @Column(nullable = true)
  public Long ownerId;

  @Column(nullable = false)
  public boolean active;

  @Column(nullable = false)
  public boolean taxRegistered;
}
