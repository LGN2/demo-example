package om.bayt.service;

import om.bayt.api.*;
import om.bayt.domain.*;
import om.bayt.security.Access;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;

@Service
@Transactional
public class PropertyService {
    private final Store db; private final Access access; private final Audit audit; private final UnitRepository units; private final Clock clock;
    public PropertyService(Store db,Access access,Audit audit,UnitRepository units,Clock clock){this.db=db;this.access=access;this.audit=audit;this.units=units;this.clock=clock;}
    public List<Building> buildings(){var ids=access.buildings();return ids.isEmpty()?List.of():db.list(Building.class,"select b from Building b where b.id in :ids order by b.id","ids",ids);}
    public Building building(Input in, Long id) {
        Building b;
        if(id==null){access.role("OWNER");b=new Building();b.ownerId=access.user().id;}else b=access.owner(id);
        b.name=in.text("name",255);b.wilayat=in.text("wilayat",255);b.address=in.text("address",255);
        b.investmentValue=in.has("investmentValue")?in.money("investmentValue",true):null;
        b.reminderDays=in.integer("reminderDays",1,365,90);b.seasonalStart=in.integer("seasonalStart",0,12,5);b.seasonalEnd=in.integer("seasonalEnd",0,12,9);
        db.save(b);audit.add(b.id,"BUILDING",b.id,id==null?"CREATED":"UPDATED","");return b;
    }
    public List<Unit> units(Long building) {
        var ids=scope(building);if(ids.isEmpty())return List.of();
        if(access.user().role.equals("TENANT"))return db.list(Unit.class,"select distinct u from Unit u, Lease l, Tenant t where u.id=l.unitId and l.tenantId=t.id and t.accountId=:user and u.buildingId in :ids order by u.id","user",access.user().id,"ids",ids);
        if(access.user().role.equals("VENDOR"))return db.list(Unit.class,"select distinct u from Unit u, Maintenance m where m.unitId=u.id and m.assignedTo=:user and u.buildingId in :ids order by u.id","user",access.user().id,"ids",ids);
        return db.list(Unit.class,"select u from Unit u where u.buildingId in :ids order by u.id","ids",ids);
    }
    public List<Long> scope(Long building){var ids=access.buildings();if(building!=null){access.building(building);return List.of(building);}return ids;}
    public Unit unit(Input in, Long id) {
        Long bid=in.id("buildingId");access.manage(bid);
        Unit u=id==null?new Unit():db.lock(Unit.class,id);if(id!=null&&!u.buildingId.equals(bid))throw ApiException.invalid("INVALID_INPUT");
        u.buildingId=bid;u.code=in.text("code",60);u.floorName=in.text("floorName",80);u.size=in.money("size",true);
        u.kind=in.choice("kind","RESIDENTIAL","COMMERCIAL");u.availability=in.choice("availability","AVAILABLE","MAINTENANCE");u.marketRent=in.money("marketRent",false);
        if(id==null)u.vacancySince=LocalDate.now(clock);u.listing=in.has("listing")?in.text("listing",5000):"";
        db.save(u);audit.add(bid,"UNIT",u.id,id==null?"CREATED":"UPDATED","");return u;
    }
    public List<Tenant> tenants(Long building) {
        access.role("OWNER","MANAGER","TENANT");var ids=scope(building);if(ids.isEmpty())return List.of();
        if(access.user().role.equals("TENANT"))return db.list(Tenant.class,"select t from Tenant t where t.accountId=:id and t.buildingId in :ids order by t.id","id",access.user().id,"ids",ids);
        return db.list(Tenant.class,"select t from Tenant t where t.buildingId in :ids order by t.id","ids",ids);
    }
    public Tenant tenant(Input in,Long id) {
        Long bid=in.id("buildingId");access.manage(bid);Tenant t=id==null?new Tenant():db.get(Tenant.class,id);
        if(id!=null&&!t.buildingId.equals(bid))throw ApiException.invalid("INVALID_INPUT");
        t.buildingId=bid;t.name=in.text("name",255);t.kind=in.choice("kind","PERSON","COMPANY");t.email=in.optional("email");t.phone=in.text("phone",80);t.emergencyContact=in.optional("emergencyContact");
        Long account=in.nullableId("accountId");if(account!=null)access.portfolioUser(account,bid,"TENANT");
        if(id!=null&&!Objects.equals(account,t.accountId)) {access.owner(bid);} // Identity reassignment is owner-only.
        t.accountId=account;db.save(t);audit.add(bid,"TENANT",t.id,id==null?"CREATED":"UPDATED","");return t;
    }
    public List<TaxPolicy> taxes(Long building) {access.role("OWNER","MANAGER");var ids=scope(building);return ids.isEmpty()?List.of():db.list(TaxPolicy.class,"select p from TaxPolicy p where p.buildingId in :ids order by p.effectiveFrom","ids",ids);}
    public TaxPolicy tax(Input in) {
        Long bid=in.id("buildingId");access.owner(bid);db.lock(Building.class,bid);
        TaxPolicy p=new TaxPolicy();p.buildingId=bid;p.treatment=in.choice("treatment","STANDARD","ZERO_RATED","EXEMPT","OUT_OF_SCOPE");p.supplyClassification=in.text("supplyClassification",255);p.rate=in.rate("rate");p.effectiveFrom=in.date("effectiveFrom");p.effectiveTo=in.optionalDate("effectiveTo");
        if(p.effectiveTo!=null&&p.effectiveTo.isBefore(p.effectiveFrom))throw ApiException.invalid("INVALID_DATE");
        if(!p.treatment.equals("STANDARD")&&p.rate.signum()!=0)throw ApiException.invalid("INVALID_TAX_POLICY");
        if(p.treatment.equals("STANDARD")&&(!access.user().taxRegistered||p.rate.signum()==0))throw ApiException.invalid("INVALID_TAX_POLICY");
        for(var old:taxes(bid))if(old.supplyClassification.equals(p.supplyClassification)&&!p.effectiveFrom.isAfter(old.effectiveTo==null?LocalDate.MAX:old.effectiveTo)&&!(p.effectiveTo==null?LocalDate.MAX:p.effectiveTo).isBefore(old.effectiveFrom))throw ApiException.conflict("TAX_POLICY_OVERLAP");
        db.save(p);audit.add(bid,"TAX_POLICY",p.id,"CREATED",p.treatment);return p;
    }
    public List<Lease> leases(Long building) {
        access.role("OWNER","MANAGER","TENANT");var ids=scope(building);if(ids.isEmpty())return List.of();
        if(access.user().role.equals("TENANT"))return db.list(Lease.class,"select l from Lease l,Tenant t where l.tenantId=t.id and t.accountId=:id and l.buildingId in :ids order by l.startDate desc","id",access.user().id,"ids",ids);
        return db.list(Lease.class,"select l from Lease l where l.buildingId in :ids order by l.startDate desc","ids",ids);
    }
    public Lease lease(Input in,Long previousId) {
        Long unitId=in.id("unitId");Unit before=db.get(Unit.class,unitId);access.manage(before.buildingId);
        Unit u=units.lockById(unitId).orElseThrow(ApiException::missing);
        if(!u.availability.equals("AVAILABLE"))throw ApiException.conflict("UNIT_UNAVAILABLE");
        Tenant t=db.get(Tenant.class,in.id("tenantId"));if(!t.buildingId.equals(u.buildingId))throw ApiException.invalid("INVALID_TENANT");
        LocalDate start=in.date("startDate");int months=in.integer("months",1,60,12);LocalDate end=start.plusMonths(months).minusDays(1);
        var existing=db.list(Lease.class,"select l from Lease l where l.unitId=:id and l.startDate<=:end and l.endDate>=:start","id",u.id,"start",start,"end",end);
        for(var l:existing){LocalDate actualEnd=l.terminatedOn==null?l.endDate:l.terminatedOn;if(!actualEnd.isBefore(start))throw ApiException.conflict("LEASE_OVERLAP");}
        TaxPolicy policy=db.get(TaxPolicy.class,in.id("taxPolicyId"));
        if(!policy.buildingId.equals(u.buildingId)||start.isBefore(policy.effectiveFrom)||(policy.effectiveTo!=null&&end.isAfter(policy.effectiveTo)))throw ApiException.invalid("INVALID_TAX_POLICY");
        Building b=db.get(Building.class,u.buildingId);boolean registered=db.get(UserAccount.class,b.ownerId).taxRegistered;
        if(policy.treatment.equals("STANDARD")&&!registered)throw ApiException.invalid("INVALID_TAX_POLICY");
        Lease l=new Lease();l.buildingId=u.buildingId;l.unitId=u.id;l.tenantId=t.id;l.startDate=start;l.endDate=end;l.rent=in.money("rent",true);l.deposit=in.money("deposit",false);l.status="ACTIVE";l.taxTreatment=policy.treatment;l.taxRate=policy.rate;l.supplyClassification=policy.supplyClassification;l.ownerTaxRegistered=registered;l.municipalityStatus="UNREGISTERED";
        if(previousId!=null){var previous=access.lease(previousId,true);if(!previous.unitId.equals(u.id)||!previous.tenantId.equals(t.id)||!start.isAfter(previous.terminatedOn==null?previous.endDate:previous.terminatedOn))throw ApiException.invalid("INVALID_RENEWAL");l.previousLeaseId=previous.id;}
        db.save(l);
        for(int i=0;i<months;i++){Due d=new Due();d.leaseId=l.id;d.dueDate=start.plusMonths(i);d.rentAmount=l.rent;d.taxAmount=l.rent.multiply(l.taxRate).setScale(3,RoundingMode.HALF_UP);d.amount=d.rentAmount.add(d.taxAmount);db.save(d);}
        audit.add(l.buildingId,"LEASE",l.id,previousId==null?"CREATED":"RENEWED","Fixed monthly rent; full monthly periods");return l;
    }
    public Lease municipality(Long id,Input in) {
        Lease l=access.lease(id,true);l.municipalityStatus=in.choice("municipalityStatus","UNREGISTERED","SUBMITTED","REGISTERED");
        l.municipalityAuthority=in.optional("municipalityAuthority");l.municipalityReference=in.optional("municipalityReference");l.municipalityFee=in.money("municipalityFee",false);
        if(l.municipalityStatus.equals("REGISTERED")&&(l.municipalityReference.isBlank()||l.municipalityAuthority.isBlank()))throw ApiException.invalid("INVALID_INPUT");
        audit.add(l.buildingId,"LEASE",id,"MUNICIPALITY_UPDATED",l.municipalityStatus);return l;
    }
    public Lease terminate(Long id,Input in) {
        Lease initial=access.lease(id,true);db.lock(Unit.class,initial.unitId);Lease l=db.lock(Lease.class,id);
        LocalDate when=in.date("terminatedOn");if(!l.status.equals("ACTIVE")||when.isBefore(l.startDate)||when.isAfter(l.endDate)||when.isAfter(LocalDate.now(clock)))throw ApiException.invalid("INVALID_DATE");
        var future=db.list(Due.class,"select d from Due d where d.leaseId=:l and d.dueDate>:date","l",id,"date",when);
        for(var due:future){var paid=db.list(Allocation.class,"select a from Allocation a, Payment p where a.paymentId=p.id and a.dueId=:d and p.reversedOn is null","d",due.id);if(!paid.isEmpty())throw ApiException.conflict("REVERSE_FUTURE_PAYMENTS_FIRST");due.cancelled=true;}
        l.status="TERMINATED";l.terminatedOn=when;db.get(Unit.class,l.unitId).vacancySince=when.plusDays(1);
        audit.add(l.buildingId,"LEASE",id,"TERMINATED",in.text("reason",255));return l;
    }
    public List<AuditEvent> history(String type,Long id){
        Long bid=switch(type){case "LEASE"->access.lease(id,false).buildingId;case "MAINTENANCE"->access.maintenance(id).buildingId;case "BUILDING"->access.building(id).id;default->throw ApiException.invalid("INVALID_INPUT");};
        if(type.equals("BUILDING"))access.staffRead(bid);
        return db.list(AuditEvent.class,"select a from AuditEvent a where a.resourceType=:type and a.resourceId=:id order by a.createdAt","type",type,"id",id);
    }
}
