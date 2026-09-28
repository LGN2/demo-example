package com.codevictims.propertymanagement.common.entity;



import jakarta.persistence.*;
import java.time.Instant;

@MappedSuperclass
public abstract class Row {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Version public long version;

  @Column(nullable = false)
  public Instant createdAt = Instant.now();
}
