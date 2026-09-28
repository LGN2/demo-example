package com.codevictims.propertymanagement.common.repository;

import com.codevictims.propertymanagement.common.entity.BaseEntity;
import com.codevictims.propertymanagement.common.exception.ApiException;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class PersistenceSupport {
  @PersistenceContext private EntityManager em;
  private final RepositoryCatalog repositories;

  public PersistenceSupport(RepositoryCatalog repositories) {
    this.repositories = repositories;
  }

  public <T extends BaseEntity> T get(Class<T> type, Long id) {
    T row = id == null ? null : repositories.repository(type).findById(id).orElse(null);
    if (row == null) throw ApiException.missing();
    return row;
  }

  @SuppressWarnings("unchecked")
  public <T extends BaseEntity> T save(T row) {
    if (row.id == null) repositories.repository((Class<T>) row.getClass()).save(row);
    return row;
  }

  public <T> List<T> list(Class<T> type, String jpql, Object... params) {
    TypedQuery<T> q = em.createQuery(jpql, type);
    for (int i = 0; i < params.length; i += 2) q.setParameter(params[i].toString(), params[i + 1]);
    return q.getResultList();
  }

  public <T extends BaseEntity> T lock(Class<T> type, Long id) {
    // Refresh under the lock, even when a pre-authorization read already loaded this row.
    T row = em.find(type, id);
    if (row == null) throw ApiException.missing();
    em.refresh(row, LockModeType.PESSIMISTIC_WRITE);
    return row;
  }

  public void remove(BaseEntity row) {
    em.remove(row);
  }

  public void flush() {
    em.flush();
  }
}
