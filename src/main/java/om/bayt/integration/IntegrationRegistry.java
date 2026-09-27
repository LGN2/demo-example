package om.bayt.integration;

import java.util.*;
import om.bayt.security.Access;
import om.bayt.service.AiAssistant;
import org.springframework.web.bind.annotation.*;

@RestController
public class IntegrationRegistry {
  public record State(String key, boolean available, String activationRequirement)
      implements ExternalIntegration {}

  private final AiAssistant ai;
  private final Access access;

  public IntegrationRegistry(AiAssistant ai, Access access) {
    this.ai = ai;
    this.access = access;
  }

  @GetMapping("/api/integrations")
  public List<State> status() {
    access.role("OWNER", "MANAGER");
    List<State> result = new ArrayList<>();
    result.add(
        new State(
            "ai",
            ai.available(),
            "Server-side AI_API_KEY and AI_MODEL; provider access and reviewed data handling."));
    for (String key :
        List.of(
            "gateway",
            "whatsapp",
            "portals",
            "locks",
            "cctv",
            "chillers",
            "lifts",
            "tanks",
            "energy"))
      result.add(
          new State(
              key,
              false,
              "Selected provider, official API documentation, credentials, sandbox/hardware access,"
                  + " and provider-specific adapter tests. No adapter is installed."));
    return result;
  }
}
