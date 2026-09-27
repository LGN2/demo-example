package om.bayt.api;

import java.util.*;
import om.bayt.security.Access;
import om.bayt.service.PropertyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PropertyController {
  private final PropertyService s;
  private final Access access;

  public PropertyController(PropertyService s, Access access) {
    this.s = s;
    this.access = access;
  }

  @GetMapping("/buildings")
  Object buildings() {
    var all = s.buildings();
    if (Set.of("OWNER", "MANAGER").contains(access.user().role)) return all;
    return all.stream()
        .map(b -> Map.of("id", b.id, "name", b.name, "wilayat", b.wilayat, "address", b.address))
        .toList();
  }

  @PostMapping("/buildings")
  Object building(@RequestBody Map<String, Object> b) {
    return s.building(new Input(b), null);
  }

  @PutMapping("/buildings/{id}")
  Object building(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.building(new Input(b), id);
  }

  @GetMapping("/units")
  Object units(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    var result =
        PageSlice.of(
            s.units(buildingId),
            page,
            size,
            q,
            u -> u.code + " " + u.floorName + " " + u.kind + " " + u.availability);
    if (Set.of("GUARD", "VENDOR").contains(access.user().role))
      return new PageSlice<>(
          result.items().stream().map(this::operationalUnit).toList(),
          result.total(),
          result.page(),
          result.size());
    return result;
  }

  @GetMapping("/units/{id}")
  Object unit(@PathVariable Long id) {
    var u = access.unit(id);
    return Set.of("GUARD", "VENDOR").contains(access.user().role) ? operationalUnit(u) : u;
  }

  private Map<String, Object> operationalUnit(om.bayt.domain.Unit u) {
    return Map.of(
        "id",
        u.id,
        "buildingId",
        u.buildingId,
        "code",
        u.code,
        "floorName",
        u.floorName,
        "kind",
        u.kind,
        "availability",
        u.availability);
  }

  @PostMapping("/units")
  Object unit(@RequestBody Map<String, Object> b) {
    return s.unit(new Input(b), null);
  }

  @PutMapping("/units/{id}")
  Object unit(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.unit(new Input(b), id);
  }

  @GetMapping("/tenants")
  Object tenants(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    return PageSlice.of(
        s.tenants(buildingId), page, size, q, t -> t.name + " " + t.phone + " " + t.kind);
  }

  @PostMapping("/tenants")
  Object tenant(@RequestBody Map<String, Object> b) {
    return s.tenant(new Input(b), null);
  }

  @PutMapping("/tenants/{id}")
  Object tenant(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.tenant(new Input(b), id);
  }

  @GetMapping("/leases")
  Object leases(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    return PageSlice.of(
        s.leases(buildingId),
        page,
        size,
        q,
        l ->
            l.id + " " + l.unitId + " " + l.tenantId + " " + l.status + " " + l.municipalityStatus);
  }

  @GetMapping("/leases/{id}")
  Object lease(@PathVariable Long id) {
    return access.lease(id, false);
  }

  @PostMapping("/leases")
  Object lease(@RequestBody Map<String, Object> b) {
    return s.lease(new Input(b), null);
  }

  @PostMapping("/leases/{id}/renew")
  Object renew(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.lease(new Input(b), id);
  }

  @PostMapping("/leases/{id}/terminate")
  Object terminate(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.terminate(id, new Input(b));
  }

  @PutMapping("/leases/{id}/municipality")
  Object municipality(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.municipality(id, new Input(b));
  }

  @GetMapping("/tax-policies")
  Object taxes(@RequestParam(required = false) Long buildingId) {
    return s.taxes(buildingId);
  }

  @PostMapping("/tax-policies")
  Object tax(@RequestBody Map<String, Object> b) {
    return s.tax(new Input(b));
  }

  @GetMapping("/history/{type}/{id}")
  Object history(@PathVariable String type, @PathVariable Long id) {
    return s.history(type, id);
  }
}
