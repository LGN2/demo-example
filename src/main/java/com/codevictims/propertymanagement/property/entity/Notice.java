package com.codevictims.propertymanagement.property.entity;

import com.codevictims.propertymanagement.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "notice")
public class Notice extends BaseEntity {
  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = false)
  public String titleAr = "";

  @Column(nullable = false)
  public String titleEn = "";

  @Column(nullable = false, columnDefinition = "TEXT")
  public String bodyAr = "";

  @Column(nullable = false, columnDefinition = "TEXT")
  public String bodyEn = "";
}
