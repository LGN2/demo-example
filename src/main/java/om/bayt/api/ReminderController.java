package om.bayt.api;

import om.bayt.service.ReminderService;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReminderController {
  private final ReminderService reminders;

  public ReminderController(ReminderService reminders) {
    this.reminders = reminders;
  }

  @GetMapping("/api/reminders")
  Object reminders(@RequestParam(required = false) Long buildingId) {
    return reminders.reminders(buildingId);
  }
}
