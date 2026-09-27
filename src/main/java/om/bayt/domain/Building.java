package om.bayt.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "building")
public class Building extends Row {
  @Column(nullable = false)
  public Long ownerId;

  @Column(nullable = false)
  public String name = "";

  @Column(nullable = false)
  public String wilayat = "";

  @Column(nullable = false)
  public String address = "";

  @Column(nullable = true, precision = 15, scale = 3)
  public BigDecimal investmentValue;

  @Column(nullable = false)
  public int reminderDays;

  @Column(nullable = false)
  public int seasonalStart;

  @Column(nullable = false)
  public int seasonalEnd;
}
