package com.codevictims.propertymanagement.common.service;

import com.codevictims.propertymanagement.common.dto.Input;
import com.codevictims.propertymanagement.common.entity.Document;
import com.codevictims.propertymanagement.common.exception.ApiException;
import com.codevictims.propertymanagement.common.repository.DocumentRepository;
import com.codevictims.propertymanagement.common.repository.PersistenceSupport;
import com.codevictims.propertymanagement.maintenance.entity.Maintenance;
import com.codevictims.propertymanagement.property.entity.Meter;
import com.codevictims.propertymanagement.property.entity.MeterReading;
import com.codevictims.propertymanagement.security.service.Access;
import com.codevictims.propertymanagement.tenancy.entity.Tenant;
import com.codevictims.propertymanagement.tenancy.repository.TenantRepository;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class FileVault {
  private final DocumentRepository documentRepository;
  private final TenantRepository tenantRepository;
  private final PersistenceSupport db;
  private final Access access;
  private final AuditService audit;
  private final Path root;
  private final String scanner;
  private final boolean dev;

  public FileVault(
      PersistenceSupport db,
      Access access,
      AuditService audit,
      @Value("${app.storage}") String root,
      @Value("${app.upload-scan-command:}") String scanner,
      @Value("${app.allow-unscanned-uploads:false}") boolean dev,
      DocumentRepository documentRepository,
      TenantRepository tenantRepository) {
    this.documentRepository = documentRepository;
    this.tenantRepository = tenantRepository;
    this.db = db;
    this.access = access;
    this.audit = audit;
    this.root = Path.of(root).toAbsolutePath().normalize();
    this.scanner = scanner;
    this.dev = dev;
  }

  public List<Document> list(Long building) {
    access.role("OWNER", "MANAGER", "TENANT", "VENDOR");
    List<Document> result = new ArrayList<>();
    var ids = access.buildings();
    if (building != null) {
      access.building(building);
      ids = List.of(building);
    }
    if (ids.isEmpty()) return result;
    for (var d : documentRepository.findInBuildingsNewestFirst(ids)) if (visible(d)) result.add(d);
    return result;
  }

  private boolean visible(Document d) {
    if (access.user().role.equals("TENANT")) {
      if (d.tenantId != null
          && Objects.equals(db.get(Tenant.class, d.tenantId).accountId, access.user().id))
        return true;
      if (d.maintenanceId != null) {
        Maintenance m = db.get(Maintenance.class, d.maintenanceId);
        return m.tenantId != null
            && Objects.equals(db.get(Tenant.class, m.tenantId).accountId, access.user().id);
      }
      return false;
    }
    if (access.user().role.equals("VENDOR"))
      return d.kind.equals("PHOTO")
          && d.maintenanceId != null
          && Objects.equals(
              db.get(Maintenance.class, d.maintenanceId).assignedTo, access.user().id);
    return Set.of("OWNER", "MANAGER").contains(access.user().role)
        && access.buildings().contains(d.buildingId);
  }

  public Document get(Long id) {
    Document d = db.get(Document.class, id);
    if (!visible(d)) throw ApiException.missing();
    return d;
  }

  public Document upload(Input in, MultipartFile file) throws IOException {
    access.role("OWNER", "MANAGER", "TENANT");
    Long bid = in.id("buildingId");
    access.building(bid);
    if (!access.user().role.equals("TENANT")) access.manage(bid);
    Document d = new Document();
    d.buildingId = bid;
    d.unitId = in.nullableId("unitId");
    d.tenantId = in.nullableId("tenantId");
    d.maintenanceId = in.nullableId("maintenanceId");
    d.readingId = in.nullableId("readingId");
    d.expiryDate = in.optionalDate("expiryDate");
    d.kind =
        in.choice(
            "kind",
            "ID",
            "CR",
            "TITLE_DEED",
            "PERMIT",
            "COMPLETION",
            "INSURANCE",
            "FIRE_EXTINGUISHER",
            "FIRE_ALARM",
            "CIVIL_DEFENCE",
            "LIFT_INSPECTION",
            "SERVICE_CONTRACT",
            "PHOTO",
            "OTHER");
    if (d.unitId != null && !access.unit(d.unitId).buildingId.equals(bid))
      throw ApiException.invalid("INVALID_INPUT");
    if (d.tenantId != null && !access.tenant(d.tenantId).buildingId.equals(bid))
      throw ApiException.invalid("INVALID_INPUT");
    if (d.maintenanceId != null) {
      Maintenance m = access.maintenance(d.maintenanceId);
      if (!m.buildingId.equals(bid)
          || !d.kind.equals("PHOTO")
          || (d.unitId != null && !d.unitId.equals(m.unitId))
          || (d.tenantId != null && !Objects.equals(d.tenantId, m.tenantId)))
        throw ApiException.invalid("INVALID_INPUT");
      d.unitId = m.unitId;
      d.tenantId = m.tenantId;
    }
    if (d.readingId != null) {
      MeterReading r = db.get(MeterReading.class, d.readingId);
      Meter m = db.get(Meter.class, r.meterId);
      access.staffRead(m.buildingId);
      if (!m.buildingId.equals(bid)
          || !d.kind.equals("PHOTO")
          || (d.unitId != null && !Objects.equals(d.unitId, m.unitId)))
        throw ApiException.invalid("INVALID_INPUT");
      d.unitId = m.unitId;
    }
    if (access.user().role.equals("TENANT")) {
      if (!Set.of("ID", "CR", "PHOTO").contains(d.kind)) throw ApiException.forbidden();
      if (d.maintenanceId == null && !Set.of("ID", "CR").contains(d.kind))
        throw ApiException.forbidden();
      var tenants = tenantRepository.findByAccountAndBuilding(access.user().id, bid);
      if (tenants.isEmpty()) throw ApiException.forbidden();
      d.tenantId = tenants.get(0).id;
    }
    if (Set.of("ID", "CR").contains(d.kind) && d.tenantId == null)
      throw ApiException.invalid("INVALID_TENANT");
    if (file.isEmpty() || file.getSize() > 8 * 1024 * 1024)
      throw ApiException.invalid("FILE_TOO_LARGE");
    byte[] content = file.getBytes();
    String contentType = detect(content);
    if (d.kind.equals("PHOTO") && contentType.equals("application/pdf"))
      throw ApiException.invalid("INVALID_FILE");
    String original = Optional.ofNullable(file.getOriginalFilename()).orElse("document");
    d.filename = original.replaceAll("[\\\\/\\r\\n\\p{Cntrl}]", "_");
    if (d.filename.length() > 200) d.filename = d.filename.substring(0, 200);
    d.contentType = contentType;
    d.sizeBytes = (long) content.length;
    d.storageKey = UUID.randomUUID().toString();
    Files.createDirectories(root);
    Path target = root.resolve(d.storageKey);
    Files.write(target, content, StandardOpenOption.CREATE_NEW);
    try {
      d.scanStatus = scan(target);
      db.save(d);
      audit.add(bid, "DOCUMENT", d.id, "UPLOADED", d.kind);
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
              if (status != STATUS_COMMITTED)
                try {
                  Files.deleteIfExists(target);
                } catch (IOException ignored) {
                }
            }
          });
      return d;
    } catch (RuntimeException e) {
      Files.deleteIfExists(target);
      throw e;
    }
  }

  String detect(byte[] b) {
    if (b.length >= 8
        && b[0] == (byte) 0x89
        && b[1] == 'P'
        && b[2] == 'N'
        && b[3] == 'G'
        && b[4] == 13
        && b[5] == 10
        && b[6] == 26
        && b[7] == 10) return "image/png";
    if (b.length >= 3 && b[0] == (byte) 0xff && b[1] == (byte) 0xd8 && b[2] == (byte) 0xff)
      return "image/jpeg";
    if (b.length >= 5
        && new String(b, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))
      return "application/pdf";
    throw ApiException.invalid("INVALID_FILE");
  }

  private String scan(Path path) {
    if (scanner.isBlank()) {
      if (dev) return "DEV_UNSCANNED";
      throw new ApiException(503, "SCAN_UNAVAILABLE");
    }
    try {
      Process p =
          new ProcessBuilder(scanner, path.toString())
              .redirectOutput(ProcessBuilder.Redirect.DISCARD)
              .redirectError(ProcessBuilder.Redirect.DISCARD)
              .start();
      if (!p.waitFor(20, TimeUnit.SECONDS)) {
        p.destroyForcibly();
        throw new ApiException(503, "SCAN_UNAVAILABLE");
      }
      if (p.exitValue() != 0) throw ApiException.invalid("SCAN_FAILED");
      return "CLEAN";
    } catch (IOException e) {
      throw new ApiException(503, "SCAN_UNAVAILABLE");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(503, "SCAN_UNAVAILABLE");
    }
  }

  public Resource download(Long id) {
    Document d = get(id);
    if (!d.scanStatus.equals("CLEAN") && !(dev && d.scanStatus.equals("DEV_UNSCANNED")))
      throw ApiException.forbidden();
    Path path = root.resolve(d.storageKey).normalize();
    if (!path.startsWith(root) || !Files.isRegularFile(path)) throw ApiException.missing();
    return new FileSystemResource(path);
  }
}
