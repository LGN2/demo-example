package com.codevictims.propertymanagement.common.config;

import com.codevictims.propertymanagement.account.entity.UserAccount;
import com.codevictims.propertymanagement.account.repository.UserRepository;
import com.codevictims.propertymanagement.account.service.AccountService;
import com.codevictims.propertymanagement.billing.entity.TaxPolicy;
import com.codevictims.propertymanagement.billing.service.FinanceService;
import com.codevictims.propertymanagement.billing.service.TaxPolicyService;
import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.repository.PersistenceSupport;
import com.codevictims.propertymanagement.maintenance.entity.VendorProfile;
import com.codevictims.propertymanagement.maintenance.service.MaintenanceService;
import com.codevictims.propertymanagement.property.entity.Building;
import com.codevictims.propertymanagement.property.entity.Notice;
import com.codevictims.propertymanagement.property.entity.Unit;
import com.codevictims.propertymanagement.property.service.PropertyService;
import com.codevictims.propertymanagement.tenancy.entity.Lease;
import com.codevictims.propertymanagement.tenancy.entity.Tenant;
import com.codevictims.propertymanagement.tenancy.service.TenancyService;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DevelopmentSeed implements ApplicationRunner {
  private final TenancyService tenancyService;
  private final TaxPolicyService taxPolicyService;
  private final UserRepository users;
  private final PasswordEncoder passwords;
  private final PersistenceSupport db;
  private final PropertyService properties;
  private final AccountService accounts;
  private final FinanceService finance;
  private final MaintenanceService maintenance;
  private final Clock clock;
  private final String password;

  public DevelopmentSeed(
      UserRepository users,
      PasswordEncoder passwords,
      PersistenceSupport db,
      PropertyService properties,
      AccountService accounts,
      FinanceService finance,
      MaintenanceService maintenance,
      Clock clock,
      @Value("${app.demo-password}") String password,
      TenancyService tenancyService,
      TaxPolicyService taxPolicyService) {

    this.tenancyService = tenancyService;
    this.taxPolicyService = taxPolicyService;
    this.users = users;
    this.passwords = passwords;
    this.db = db;
    this.properties = properties;
    this.accounts = accounts;
    this.finance = finance;
    this.maintenance = maintenance;
    this.clock = clock;
    this.password = password;
  }

  public static Input input(Object... pairs) {
    Map<String, Object> values = new HashMap<>();
    for (int i = 0; i < pairs.length; i += 2) values.put(pairs[i].toString(), pairs[i + 1]);
    return new Input(values);
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (users.count() != 0) return;
    if (password.length() < 12)
      throw new IllegalStateException("DEMO_PASSWORD must contain at least 12 characters");
    UserAccount owner = owner("owner@demo.test", "محفظة الواحة", "OWNER"),
        other = owner("owner2@demo.test", "محفظة النور", "OWNER");
    owner("admin@demo.test", "إدارة المنصة", "PLATFORM_ADMIN");
    try {
      login(owner);
      Building b =
          properties.building(
              input(
                  "name",
                  "الواحة ريزيدنس",
                  "wilayat",
                  "بوشر",
                  "address",
                  "مسقط، سلطنة عمان",
                  "investmentValue",
                  "250000.000"),
              null);
      UserAccount manager =
          accounts.create(
              input(
                  "username",
                  "manager@demo.test",
                  "displayName",
                  "مدير العقار",
                  "role",
                  "MANAGER",
                  "password",
                  password));
      UserAccount tenantUser =
          accounts.create(
              input(
                  "username",
                  "tenant@demo.test",
                  "displayName",
                  "مستأجر تجريبي",
                  "role",
                  "TENANT",
                  "password",
                  password));
      UserAccount vendor =
          accounts.create(
              input(
                  "username",
                  "vendor@demo.test",
                  "displayName",
                  "فني الصيانة",
                  "role",
                  "VENDOR",
                  "password",
                  password));
      UserAccount guard =
          accounts.create(
              input(
                  "username",
                  "guard@demo.test",
                  "displayName",
                  "حارس المبنى",
                  "role",
                  "GUARD",
                  "password",
                  password));
      accounts.create(
          input(
              "username",
              "unassigned@demo.test",
              "displayName",
              "مدير غير معين",
              "role",
              "MANAGER",
              "password",
              password));
      accounts.assign(input("buildingId", b.id, "userId", manager.id, "canWrite", true));
      accounts.assign(input("buildingId", b.id, "userId", guard.id, "canWrite", false));
      VendorProfile v = new VendorProfile();
      v.buildingId = b.id;
      v.userId = vendor.id;
      v.name = "خدمات صيانة تجريبية";
      v.categories = "AC,PLUMBING";
      v.hourlyRate = new java.math.BigDecimal("8.000");
      db.save(v);
      TaxPolicy tax =
          taxPolicyService.tax(
              input(
                  "buildingId",
                  b.id,
                  "treatment",
                  "EXEMPT",
                  "supplyClassification",
                  "DEMO classification — confirm actual treatment",
                  "rate",
                  "0",
                  "effectiveFrom",
                  "2020-01-01"));
      Unit first = null;
      for (int i = 1; i <= 8; i++) {
        Unit u =
            properties.unit(
                input(
                    "buildingId",
                    b.id,
                    "code",
                    "A-" + (100 + i),
                    "floorName",
                    i < 5 ? "الأول" : "الثاني",
                    "size",
                    "110.000",
                    "kind",
                    "RESIDENTIAL",
                    "availability",
                    i == 8 ? "MAINTENANCE" : "AVAILABLE",
                    "marketRent",
                    "300.000",
                    "listing",
                    "شقة واسعة في موقع مركزي"),
                null);
        if (first == null) first = u;
      }
      Tenant t =
          tenancyService.tenant(
              input(
                  "buildingId",
                  b.id,
                  "accountId",
                  tenantUser.id,
                  "name",
                  "مستأجر تجريبي",
                  "kind",
                  "PERSON",
                  "email",
                  "tenant@demo.test",
                  "phone",
                  "+968 00000000",
                  "emergencyContact",
                  "بيانات تجريبية فقط"),
              null);
      LocalDate start = LocalDate.now(clock).withDayOfMonth(1);
      Lease l =
          tenancyService.lease(
              input(
                  "unitId",
                  first.id,
                  "tenantId",
                  t.id,
                  "startDate",
                  start.toString(),
                  "months",
                  12,
                  "rent",
                  "300.000",
                  "deposit",
                  "300.000",
                  "taxPolicyId",
                  tax.id),
              null);
      finance.payment(
          l.id,
          input(
              "amount",
              "100.000",
              "method",
              "BANK_TRANSFER",
              "reference",
              "DEMO",
              "effectiveDate",
              start.toString(),
              "idempotencyKey",
              "seed-rent-100"));
      finance.cheque(
          l.id,
          input(
              "chequeNumber",
              "DEMO-200",
              "bank",
              "Demo bank",
              "chequeDate",
              LocalDate.now(clock).plusDays(5).toString(),
              "amount",
              "200.000"));
      finance.deposit(
          l.id,
          input(
              "kind",
              "RECEIPT",
              "amount",
              "300.000",
              "effectiveDate",
              start.toString(),
              "reason",
              "Demo security deposit",
              "idempotencyKey",
              "seed-deposit"));
      maintenance.create(
          input(
              "unitId",
              first.id,
              "tenantId",
              t.id,
              "description",
              "مكيف غرفة المعيشة لا يبرد بشكل جيد",
              "category",
              "AC",
              "urgent",
              false));
      Notice notice = new Notice();
      notice.buildingId = b.id;
      notice.titleAr = "أهلاً بكم في بيت";
      notice.titleEn = "Welcome to Bayt";
      notice.bodyAr = "هذه بيانات تجريبية. سجّل طلبات الصيانة وتابع مستحقات الإيجار من هنا.";
      notice.bodyEn = "Synthetic demonstration data. Track maintenance and rent here.";
      db.save(notice);
      login(other);
      Building b2 =
          properties.building(
              input("name", "النور للأعمال", "wilayat", "السيب", "address", "مسقط، سلطنة عمان"),
              null);
      properties.unit(
          input(
              "buildingId",
              b2.id,
              "code",
              "S-01",
              "floorName",
              "الأرضي",
              "size",
              "80.000",
              "kind",
              "COMMERCIAL",
              "availability",
              "AVAILABLE",
              "marketRent",
              "450.000"),
          null);
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  private UserAccount owner(String username, String name, String role) {
    UserAccount u = new UserAccount();
    u.username = username;
    u.displayName = name;
    u.role = role;
    u.active = true;
    u.passwordHash = passwords.encode(password);
    db.save(u);
    if (role.equals("OWNER")) u.ownerId = u.id;
    return u;
  }

  private void login(UserAccount u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u.username, "", List.of()));
  }
}
