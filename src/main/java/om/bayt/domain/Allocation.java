package om.bayt.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "allocation")
public class Allocation extends Row {
  @Column(nullable = false)
  public Long paymentId;

  @Column(nullable = false)
  public Long dueId;

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal amount = BigDecimal.ZERO;
}
