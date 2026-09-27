package om.bayt.api;

import java.time.*;
import java.util.*;
import om.bayt.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FinanceController {
  private final FinanceService s;
  private final PropertyService properties;
  private final Clock clock;

  public FinanceController(FinanceService s, PropertyService properties, Clock clock) {
    this.s = s;
    this.properties = properties;
    this.clock = clock;
  }

  @GetMapping("/leases/{id}/dues")
  Object dues(@PathVariable Long id, @RequestParam(required = false) LocalDate asOf) {
    return s.dues(id, asOf == null ? LocalDate.now(clock) : asOf);
  }

  @GetMapping("/leases/{id}/payments")
  Object payments(@PathVariable Long id) {
    return s.payments(id);
  }

  @PostMapping("/leases/{id}/payments")
  Object payment(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.payment(id, new Input(b));
  }

  @PostMapping("/payments/{id}/reverse")
  Object reverse(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.reverse(id, new Input(b));
  }

  @GetMapping("/leases/{id}/cheques")
  Object cheques(@PathVariable Long id) {
    return s.cheques(id);
  }

  @PostMapping("/leases/{id}/cheques")
  Object cheque(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.cheque(id, new Input(b));
  }

  @PostMapping("/cheques/{id}/status")
  Object chequeStatus(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.transitionCheque(id, new Input(b));
  }

  @GetMapping("/leases/{id}/deposits")
  Object deposits(@PathVariable Long id) {
    return Map.of("entries", s.deposits(id), "held", s.held(id));
  }

  @PostMapping("/leases/{id}/deposits")
  Object deposit(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.deposit(id, new Input(b));
  }

  @GetMapping("/leases/{id}/follow-ups")
  Object followUps(@PathVariable Long id) {
    return s.followUps(id);
  }

  @PostMapping("/leases/{id}/follow-ups")
  Object followUp(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.followUp(id, new Input(b));
  }

  @GetMapping("/expenses")
  Object expenses(
      @RequestParam(required = false) Long buildingId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String q) {
    return PageSlice.of(
        s.expenses(properties.scope(buildingId)),
        page,
        size,
        q,
        e -> e.category + " " + e.description);
  }

  @PostMapping("/expenses")
  Object expense(@RequestBody Map<String, Object> b) {
    return s.expense(new Input(b));
  }

  @PostMapping("/expenses/{id}/reverse")
  Object reverseExpense(@PathVariable Long id, @RequestBody Map<String, Object> b) {
    return s.reverseExpense(id, new Input(b));
  }
}
