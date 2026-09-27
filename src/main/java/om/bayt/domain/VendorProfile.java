package om.bayt.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "vendor_profile")
public class VendorProfile extends Row {
  @Column(nullable = false)
  public Long buildingId;

  @Column(nullable = false)
  public Long userId;

  @Column(nullable = false)
  public String name = "";

  @Column(nullable = false)
  public String categories = "";

  @Column(nullable = false, precision = 15, scale = 3)
  public BigDecimal hourlyRate = BigDecimal.ZERO;
}
