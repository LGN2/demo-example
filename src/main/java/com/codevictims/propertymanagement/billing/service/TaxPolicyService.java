package com.codevictims.propertymanagement.billing.service;

import com.codevictims.propertymanagement.account.entity.UserAccount;
import com.codevictims.propertymanagement.billing.entity.Allocation;
import com.codevictims.propertymanagement.billing.entity.Due;
import com.codevictims.propertymanagement.billing.entity.Payment;
import com.codevictims.propertymanagement.billing.entity.TaxPolicy;
import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.entity.AuditEvent;
import com.codevictims.propertymanagement.common.exception.ApiException;
import com.codevictims.propertymanagement.common.repository.Store;
import com.codevictims.propertymanagement.common.service.Audit;
import com.codevictims.propertymanagement.maintenance.entity.Maintenance;
import com.codevictims.propertymanagement.property.entity.Building;
import com.codevictims.propertymanagement.property.entity.Unit;
import com.codevictims.propertymanagement.property.repository.UnitRepository;
import com.codevictims.propertymanagement.security.service.Access;
import com.codevictims.propertymanagement.tenancy.entity.Lease;
import com.codevictims.propertymanagement.tenancy.entity.Tenant;

import com.codevictims.propertymanagement.property.service.PropertyService;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class TaxPolicyService {
  private final Store db;
  private final Access access;
  private final Audit audit;
  private final UnitRepository units;
  private final Clock clock;
  private final PropertyService properties;

  public TaxPolicyService(Store db, Access access, Audit audit, UnitRepository units, Clock clock, PropertyService properties) {
    this.db = db;
    this.access = access;
    this.audit = audit;
    this.units = units;
    this.clock = clock;
    this.properties = properties;
  }

  public List<TaxPolicy> taxes(Long building) {
    access.role("OWNER", "MANAGER");
    var ids = properties.scope(building);
    return ids.isEmpty()
        ? List.of()
        : db.list(
            TaxPolicy.class,
            "select p from TaxPolicy p where p.buildingId in :ids order by p.effectiveFrom",
            "ids",
            ids);
  }

  public TaxPolicy tax(Input in) {
    Long bid = in.id("buildingId");
    access.owner(bid);
    db.lock(Building.class, bid);
    TaxPolicy p = new TaxPolicy();
    p.buildingId = bid;
    p.treatment = in.choice("treatment", "STANDARD", "ZERO_RATED", "EXEMPT", "OUT_OF_SCOPE");
    p.supplyClassification = in.text("supplyClassification", 255);
    p.rate = in.rate("rate");
    p.effectiveFrom = in.date("effectiveFrom");
    p.effectiveTo = in.optionalDate("effectiveTo");
    if (p.effectiveTo != null && p.effectiveTo.isBefore(p.effectiveFrom))
      throw ApiException.invalid("INVALID_DATE");
    if (!p.treatment.equals("STANDARD") && p.rate.signum() != 0)
      throw ApiException.invalid("INVALID_TAX_POLICY");
    if (p.treatment.equals("STANDARD") && (!access.user().taxRegistered || p.rate.signum() == 0))
      throw ApiException.invalid("INVALID_TAX_POLICY");
    for (var old : taxes(bid))
      if (old.supplyClassification.equals(p.supplyClassification)
          && !p.effectiveFrom.isAfter(old.effectiveTo == null ? LocalDate.MAX : old.effectiveTo)
          && !(p.effectiveTo == null ? LocalDate.MAX : p.effectiveTo).isBefore(old.effectiveFrom))
        throw ApiException.conflict("TAX_POLICY_OVERLAP");
    db.save(p);
    audit.add(bid, "TAX_POLICY", p.id, "CREATED", p.treatment);
    return p;
  }

}
