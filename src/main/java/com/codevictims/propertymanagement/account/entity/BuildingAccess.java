package com.codevictims.propertymanagement.account.entity;

import com.codevictims.propertymanagement.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "building_access")
public class BuildingAccess extends BaseEntity {
  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = false)
  public Long userId;

  @Column(nullable = false)
  public boolean canWrite;
}
