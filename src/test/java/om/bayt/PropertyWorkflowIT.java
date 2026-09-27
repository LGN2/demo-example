package om.bayt;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import om.bayt.api.*;
import om.bayt.domain.*;
import om.bayt.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Runs against actual MySQL. No embedded database, docker detection, or silent skip. */
@SpringBootTest(
    properties = {
      "server.servlet.session.cookie.secure=false",
      "app.allow-unscanned-uploads=true",
      "app.storage=target/test-vault"
    })
@AutoConfigureMockMvc
class PropertyWorkflowIT {
  @Autowired Store db;
  @Autowired UserRepository users;
  @Autowired PropertyService properties;
  @Autowired FinanceService finance;
  @Autowired AccountService accounts;
  @Autowired MaintenanceService maintenance;
  @Autowired OperationsService operations;
  @Autowired ReportService reports;
  @Autowired FileVault vault;
  @Autowired AiAssistant ai;
  @Autowired PasswordEncoder passwords;
  @Autowired PlatformTransactionManager transactionManager;
  @Autowired MockMvc mvc;
  @Autowired Clock clock;
  UserAccount owner, owner2, tenantUser, tenant2, manager, vendor, guard, admin;
  Building building;
  Unit unit;
  Tenant tenant;
  TaxPolicy policy;
  Lease lease;
  LocalDate today;

  static Input in(Object... args) {
    return DevelopmentSeed.input(args);
  }

  void login(UserAccount u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u.username, "", List.of()));
  }

  @BeforeEach
  void fixture() {
    today = LocalDate.now(clock);
    new TransactionTemplate(transactionManager)
        .executeWithoutResult(
            tx -> {
              String suffix = UUID.randomUUID().toString().substring(0, 8);
              owner = account("OWNER", "owner-" + suffix, null);
              owner.ownerId = owner.id;
              owner2 = account("OWNER", "other-" + suffix, null);
              owner2.ownerId = owner2.id;
              tenantUser = account("TENANT", "tenant-" + suffix, owner.id);
              tenant2 = account("TENANT", "tenant2-" + suffix, owner.id);
              manager = account("MANAGER", "manager-" + suffix, owner.id);
              vendor = account("VENDOR", "vendor-" + suffix, owner.id);
              guard = account("GUARD", "guard-" + suffix, owner.id);
              admin = account("PLATFORM_ADMIN", "admin-" + suffix, null);
            });
    login(owner);
    building =
        properties.building(
            in("name", "Test building", "wilayat", "Muscat", "address", "Synthetic address"), null);
    unit =
        properties.unit(
            in(
                "buildingId",
                building.id,
                "code",
                "A1",
                "floorName",
                "1",
                "size",
                "100.000",
                "kind",
                "RESIDENTIAL",
                "availability",
                "AVAILABLE",
                "marketRent",
                "300.000"),
            null);
    tenant =
        properties.tenant(
            in(
                "buildingId",
                building.id,
                "accountId",
                tenantUser.id,
                "name",
                "Synthetic tenant",
                "kind",
                "PERSON",
                "phone",
                "00000000"),
            null);
    policy =
        properties.tax(
            in(
                "buildingId",
                building.id,
                "treatment",
                "EXEMPT",
                "supplyClassification",
                "Synthetic training policy",
                "rate",
                "0",
                "effectiveFrom",
                "2020-01-01"));
    lease = properties.lease(leaseInput(unit.id, tenant.id, today), null);
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  UserAccount account(String role, String name, Long ownerId) {
    UserAccount u = new UserAccount();
    u.username = name;
    u.displayName = name;
    u.role = role;
    u.ownerId = ownerId;
    u.active = true;
    u.passwordHash = passwords.encode("TestPassword!2026");
    return db.save(u);
  }

  Input leaseInput(Long unitId, Long tenantId, LocalDate start) {
    return in(
        "unitId",
        unitId,
        "tenantId",
        tenantId,
        "startDate",
        start.toString(),
        "months",
        1,
        "rent",
        "300.000",
        "deposit",
        "300.000",
        "taxPolicyId",
        policy.id);
  }

  Input pay(String amount, String key) {
    return in(
        "amount",
        amount,
        "method",
        "BANK_TRANSFER",
        "reference",
        "TEST",
        "effectiveDate",
        today.toString(),
        "idempotencyKey",
        key);
  }

  Cheque cheque(String number) {
    return finance.cheque(
        lease.id,
        in(
            "chequeNumber",
            number,
            "bank",
            "Test bank",
            "chequeDate",
            today.toString(),
            "amount",
            "200.000"));
  }

  void transition(Cheque c, String status) {
    finance.transitionCheque(c.id, in("status", status, "effectiveDate", today.toString()));
  }

  BigDecimal outstanding() {
    return finance.dues(lease.id, today).get(0).outstanding();
  }

  @Test
  void fullFinancialAcceptanceAndDashboardReconcile() {
    finance.payment(lease.id, pay("100.000", "partial"));
    assertEquals(0, new BigDecimal("200.000").compareTo(outstanding()));
    Cheque c = cheque("bounced");
    assertEquals(0, new BigDecimal("200.000").compareTo(outstanding()));
    transition(c, "DEPOSITED");
    transition(c, "BOUNCED");
    assertEquals(0, new BigDecimal("200.000").compareTo(outstanding()));
    Cheque replacement = cheque("replacement");
    transition(replacement, "DEPOSITED");
    transition(replacement, "CLEARED");
    transition(replacement, "CLEARED");
    assertEquals(0, outstanding().signum());
    assertEquals(2, finance.payments(lease.id).size());
    finance.deposit(
        lease.id,
        in(
            "kind",
            "RECEIPT",
            "amount",
            "300.000",
            "effectiveDate",
            today.toString(),
            "reason",
            "Security deposit",
            "idempotencyKey",
            "deposit"));
    var report = reports.dashboard(building.id, null, today, today, today);
    assertEquals(
        0, new BigDecimal("300.000").compareTo((BigDecimal) report.get("settledCollections")));
    assertEquals(0, new BigDecimal("300.000").compareTo(finance.held(lease.id)));
    assertEquals(0, ((BigDecimal) report.get("overdue")).signum());
  }

  @Test
  void concurrentClearanceCreatesExactlyOneSettledPayment() throws Exception {
    finance.payment(lease.id, pay("100.000", "partial"));
    Cheque c = cheque("race");
    transition(c, "DEPOSITED");
    var pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<Long>> tasks = new ArrayList<>();
      for (int i = 0; i < 2; i++)
        tasks.add(
            pool.submit(
                () -> {
                  login(owner);
                  try {
                    start.await();
                    return finance.transitionCheque(
                            c.id, in("status", "CLEARED", "effectiveDate", today.toString()))
                        .paymentId;
                  } finally {
                    SecurityContextHolder.clearContext();
                  }
                }));
      start.countDown();
      assertEquals(tasks.get(0).get(20, TimeUnit.SECONDS), tasks.get(1).get(20, TimeUnit.SECONDS));
      assertEquals(2, finance.payments(lease.id).size());
      assertEquals(0, outstanding().signum());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void simultaneousManualPaymentsCannotOverAllocate() throws Exception {
    var pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<Boolean>> tasks = new ArrayList<>();
      for (int i = 0; i < 2; i++) {
        final int n = i;
        tasks.add(
            pool.submit(
                () -> {
                  login(owner);
                  try {
                    start.await();
                    finance.payment(lease.id, pay("200.000", "parallel-" + n));
                    return true;
                  } catch (ApiException e) {
                    assertEquals("OVERPAYMENT", e.code);
                    return false;
                  } finally {
                    SecurityContextHolder.clearContext();
                  }
                }));
      }
      start.countDown();
      int success = 0;
      for (var task : tasks) if (task.get(20, TimeUnit.SECONDS)) success++;
      assertEquals(1, success);
      assertEquals(0, new BigDecimal("100.000").compareTo(outstanding()));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void idempotencyRejectsChangedPayloadAndReversalPreservesAudit() {
    Payment p = finance.payment(lease.id, pay("100.000", "same"));
    assertEquals(p.id, finance.payment(lease.id, pay("100.000", "same")).id);
    assertEquals(
        "IDEMPOTENCY_CONFLICT",
        assertThrows(ApiException.class, () -> finance.payment(lease.id, pay("101.000", "same")))
            .code);
    finance.reverse(p.id, in("reason", "Correction"));
    finance.reverse(p.id, in("reason", "Retry"));
    assertEquals(0, new BigDecimal("300.000").compareTo(outstanding()));
    assertTrue(
        properties.history("LEASE", lease.id).stream()
            .anyMatch(e -> e.action.equals("PAYMENT_REVERSED")));
  }

  @Test
  void overlapRejectedAndRenewalCreatesIndependentDues() {
    assertEquals(
        "LEASE_OVERLAP",
        assertThrows(
                ApiException.class,
                () -> properties.lease(leaseInput(unit.id, tenant.id, today), null))
            .code);
    Lease renewed =
        properties.lease(leaseInput(unit.id, tenant.id, lease.endDate.plusDays(1)), lease.id);
    assertEquals(lease.id, renewed.previousLeaseId);
    assertEquals(1, finance.dues(renewed.id, today).size());
  }

  @Test
  void crossOwnerUnassignedManagerOtherTenantAndAdminAreDenied() throws Exception {
    for (UserAccount outsider : List.of(owner2, manager, tenant2, admin)) {
      mvc.perform(get("/api/leases/" + lease.id).with(user(outsider.username)))
          .andExpect(status().is4xxClientError());
      mvc.perform(
              post("/api/leases/" + lease.id + "/payments")
                  .with(user(outsider.username))
                  .with(csrf())
                  .contentType("application/json")
                  .content(
                      "{\"amount\":100,\"method\":\"CASH\",\"effectiveDate\":\""
                          + today
                          + "\",\"idempotencyKey\":\"deny\"}"))
          .andExpect(status().is4xxClientError());
      mvc.perform(get("/api/print/lease/" + lease.id).with(user(outsider.username)))
          .andExpect(status().is4xxClientError());
    }
    mvc.perform(get("/api/leases/" + lease.id).with(user(tenantUser.username)))
        .andExpect(status().isOk());
    mvc.perform(
            post("/api/buildings")
                .with(user(owner.username))
                .contentType("application/json")
                .content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void readOnlyManagerCanReadButNotWrite() {
    accounts.assign(in("buildingId", building.id, "userId", manager.id, "canWrite", false));
    login(manager);
    assertEquals(1, properties.leases(building.id).size());
    assertThrows(ApiException.class, () -> finance.payment(lease.id, pay("100.000", "denied")));
  }

  @Test
  void tenantsCannotSeeInvestmentValues() throws Exception {
    mvc.perform(get("/api/buildings").with(user(tenantUser.username)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].investmentValue").doesNotExist());
  }

  @Test
  void maintenanceLifecycleAndManualFallback() {
    accounts.assign(in("buildingId", building.id, "userId", manager.id, "canWrite", true));
    login(tenantUser);
    Maintenance m =
        maintenance.create(
            in(
                "unitId",
                unit.id,
                "description",
                "المكيف لا يبرد",
                "category",
                "AC",
                "urgent",
                true));
    assertThrows(
        ApiException.class,
        () -> maintenance.transition(m.id, in("status", "ASSIGNED", "assignedTo", manager.id)));
    login(owner);
    assertEquals("UNAVAILABLE", ai.suggest(m.description, "ar").status());
    maintenance.aiResult(m.id, ai.suggest(m.description, "ar"));
    assertEquals(1, maintenance.list(building.id).size());
    maintenance.transition(m.id, in("status", "ASSIGNED", "assignedTo", manager.id));
    maintenance.transition(m.id, in("status", "IN_PROGRESS"));
    maintenance.transition(m.id, in("status", "RESOLVED"));
    maintenance.transition(m.id, in("status", "CLOSED"));
    assertEquals("CLOSED", maintenance.list(building.id).get(0).status);
    assertEquals(6, properties.history("MAINTENANCE", m.id).size());
  }

  @Test
  void assignedVendorCannotReadFinancesOrAnotherJob() throws Exception {
    operations.save(
        "vendors",
        null,
        in(
            "buildingId",
            building.id,
            "userId",
            vendor.id,
            "name",
            "Synthetic vendor",
            "categories",
            "AC",
            "hourlyRate",
            "8.000"));
    Maintenance m =
        maintenance.create(
            in("unitId", unit.id, "description", "AC issue", "category", "AC", "urgent", false));
    maintenance.transition(m.id, in("status", "ASSIGNED", "assignedTo", vendor.id));
    mvc.perform(get("/api/maintenance/" + m.id).with(user(vendor.username)))
        .andExpect(status().isOk());
    mvc.perform(get("/api/leases/" + lease.id + "/payments").with(user(vendor.username)))
        .andExpect(status().isForbidden());
    login(vendor);
    maintenance.transition(m.id, in("status", "IN_PROGRESS"));
    maintenance.transition(m.id, in("status", "RESOLVED"));
    assertThrows(ApiException.class, () -> maintenance.transition(m.id, in("status", "CLOSED")));
  }

  @Test
  void depositsRejectOverRefundAndStayOutOfIncome() {
    finance.deposit(
        lease.id,
        in(
            "kind",
            "RECEIPT",
            "amount",
            "300",
            "effectiveDate",
            today.toString(),
            "reason",
            "Held",
            "idempotencyKey",
            "d1"));
    assertThrows(
        ApiException.class,
        () ->
            finance.deposit(
                lease.id,
                in(
                    "kind",
                    "REFUND",
                    "amount",
                    "301",
                    "effectiveDate",
                    today.toString(),
                    "reason",
                    "Too much",
                    "idempotencyKey",
                    "d2")));
    assertEquals(
        0,
        ((BigDecimal)
                reports.dashboard(building.id, null, today, today, today).get("settledCollections"))
            .signum());
  }

  @Test
  void taxIsExplicitAndHistoricalDuesDoNotChange() {
    accounts.tax(in("taxRegistered", true));
    TaxPolicy tax =
        properties.tax(
            in(
                "buildingId",
                building.id,
                "treatment",
                "STANDARD",
                "supplyClassification",
                "Test taxable supply",
                "rate",
                "0.05",
                "effectiveFrom",
                "2020-01-01"));
    var next =
        properties.lease(
            in(
                "unitId",
                unit.id,
                "tenantId",
                tenant.id,
                "startDate",
                lease.endDate.plusDays(1).toString(),
                "months",
                1,
                "rent",
                "300",
                "deposit",
                "0",
                "taxPolicyId",
                tax.id),
            lease.id);
    assertEquals(
        0, new BigDecimal("315.000").compareTo(finance.dues(next.id, today).get(0).amount()));
    accounts.tax(in("taxRegistered", false));
    assertEquals(
        0, new BigDecimal("315.000").compareTo(finance.dues(next.id, today).get(0).amount()));
  }

  @Test
  void meterReadingsAndGuardWorkflowsAreRestricted() {
    Meter meter =
        (Meter)
            operations.save(
                "meters",
                null,
                in(
                    "buildingId",
                    building.id,
                    "unitId",
                    unit.id,
                    "accountNumber",
                    "TEST",
                    "kind",
                    "WATER",
                    "responsibility",
                    "TENANT",
                    "commonArea",
                    false,
                    "alertThreshold",
                    "50"));
    operations.reading(
        meter.id,
        in("readingDate", today.minusDays(1).toString(), "value", "100", "kind", "MOVE_IN"));
    MeterReading reading =
        operations.reading(
            meter.id, in("readingDate", today.toString(), "value", "180", "kind", "ROUTINE"));
    assertEquals("UNUSUAL_CONSUMPTION", reading.observation);
    assertThrows(
        ApiException.class,
        () ->
            operations.reading(
                meter.id, in("readingDate", today.toString(), "value", "170", "kind", "ROUTINE")));
    accounts.assign(in("buildingId", building.id, "userId", guard.id, "canWrite", false));
    login(guard);
    Visit v =
        (Visit)
            operations.save(
                "visits",
                null,
                in(
                    "buildingId",
                    building.id,
                    "unitId",
                    unit.id,
                    "visitorName",
                    "Synthetic visitor",
                    "purpose",
                    "Delivery"));
    assertNotNull(operations.checkout(v.id).checkOut);
    assertThrows(ApiException.class, () -> operations.readings(meter.id));
    assertThrows(ApiException.class, () -> finance.payments(lease.id));
  }

  @Test
  void documentsAreProtectedAndInvalidFilesRejected() throws Exception {
    var file =
        new MockMultipartFile(
            "file", "synthetic.pdf", "application/pdf", "%PDF-1.7\nsynthetic-test-only".getBytes());
    Document d =
        vault.upload(in("buildingId", building.id, "tenantId", tenant.id, "kind", "ID"), file);
    mvc.perform(get("/api/documents/" + d.id + "/download").with(user(owner2.username)))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/documents/" + d.id + "/download").with(user(tenant2.username)))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/documents/" + d.id + "/download").with(user(tenantUser.username)))
        .andExpect(status().isOk());
    assertThrows(
        ApiException.class,
        () ->
            vault.upload(
                in("buildingId", building.id, "kind", "OTHER"),
                new MockMultipartFile(
                    "file", "bad.svg", "image/svg+xml", "<script>alert(1)</script>".getBytes())));
  }

  @Test
  void recurringTaskCompletionAdvancesWithoutDuplicateCompletion() {
    PreventiveTask p =
        (PreventiveTask)
            operations.save(
                "preventive",
                null,
                in(
                    "buildingId",
                    building.id,
                    "title",
                    "Filter cleaning",
                    "category",
                    "AC",
                    "intervalDays",
                    30,
                    "nextDue",
                    today.minusDays(35).toString(),
                    "enabled",
                    true));
    var done = operations.complete(p.id);
    assertTrue(done.nextDue.isAfter(today));
    assertThrows(ApiException.class, () -> operations.complete(p.id));
  }
  @Test
  void concurrentLeaseCreationCannotOverlap() throws Exception {
    var pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<Boolean>> attempts = new ArrayList<>();
      for (int i = 0; i < 2; i++) {
        attempts.add(pool.submit(() -> {
          login(owner);
          try {
            start.await();
            properties.lease(leaseInput(unit.id, tenant.id, lease.endDate.plusDays(1)), lease.id);
            return true;
          } catch (ApiException e) {
            assertEquals("LEASE_OVERLAP", e.code);
            return false;
          } finally {
            SecurityContextHolder.clearContext();
          }
        }));
      }
      start.countDown();
      int created = 0;
      for (var result : attempts) if (result.get(20, TimeUnit.SECONDS)) created++;
      assertEquals(1, created);
    } finally {
      pool.shutdownNow();
    }
  }
  @Test
  void tenantHistoryDoesNotExposeInternalFollowUpNotes() {
    finance.followUp(lease.id, in("note", "Internal collection discussion"));
    login(tenantUser);
    assertTrue(properties.history("LEASE", lease.id).stream().noneMatch(e -> e.action.equals("FOLLOW_UP")));
    assertThrows(ApiException.class, () -> finance.followUps(lease.id));
  }
}
