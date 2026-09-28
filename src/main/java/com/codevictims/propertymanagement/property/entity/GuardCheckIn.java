package com.codevictims.propertymanagement.property.entity;

import com.codevictims.propertymanagement.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "guard_check_in")
public class GuardCheckIn extends BaseEntity {
  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = false)
  public Long userId;

  @Column(nullable = false)
  public String note = "";
}
