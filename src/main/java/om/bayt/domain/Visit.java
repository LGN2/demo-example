package om.bayt.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "visit")
public class Visit extends Row {
  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = true)
  public Long unitId;

  @Column(nullable = false)
  public String visitorName = "";

  @Column(nullable = false)
  public String purpose = "";

  @Column(nullable = false)
  public Instant checkIn;

  @Column(nullable = true)
  public Instant checkOut;
}
