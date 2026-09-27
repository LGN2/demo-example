package om.bayt.api;

import java.util.Map;
import om.bayt.service.AccountService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AccountController {
  private final AccountService service;

  public AccountController(AccountService service) {
    this.service = service;
  }

  @GetMapping("/users")
  Object list() {
    return service.list();
  }

  @PostMapping("/users")
  Object create(@RequestBody Map<String, Object> b) {
    return service.create(new Input(b));
  }

  @GetMapping("/access")
  Object grants(@RequestParam Long buildingId) {
    return service.grants(buildingId);
  }

  @PostMapping("/access")
  Object grant(@RequestBody Map<String, Object> b) {
    return service.assign(new Input(b));
  }

  @DeleteMapping("/access/{id}")
  void revoke(@PathVariable Long id) {
    service.revoke(id);
  }

  @PostMapping("/auth/password")
  void password(@RequestBody Map<String, Object> b) {
    service.password(new Input(b));
  }

  @PutMapping("/owner/tax-status")
  Object tax(@RequestBody Map<String, Object> b) {
    return service.tax(new Input(b));
  }
}
