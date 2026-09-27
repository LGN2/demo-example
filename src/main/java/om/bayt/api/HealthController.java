package om.bayt.api;

import java.util.Map;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  private volatile boolean ready;

  @EventListener(ApplicationReadyEvent.class)
  public void ready() { ready = true; }

  @GetMapping("/api/health")
  public ResponseEntity<?> health() {
    return ResponseEntity.status(ready ? 200 : 503).body(Map.of("ready", ready));
  }
}
