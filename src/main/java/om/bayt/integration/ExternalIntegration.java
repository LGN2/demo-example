package om.bayt.integration;

/** Provider adapters must implement readiness without activating side effects in development. */
public interface ExternalIntegration {
  String key();

  boolean available();

  String activationRequirement();
}
