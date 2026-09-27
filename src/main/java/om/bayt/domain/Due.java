package om.bayt.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "due")
public class Due extends Row {
  @Column(nullable = false)
  public Long leaseId;

  @Column(nullable = false)
  public LocalDate dueDate;

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal rentAmount = BigDecimal.ZERO;

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal amount = BigDecimal.ZERO;

  @Column(nullable = false)
  public boolean cancelled;

  public LocalDate cancelledOn;
}
