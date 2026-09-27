package om.bayt.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "deposit_entry")
public class DepositEntry extends Row {
    @Column(nullable = false)
    public Long leaseId;
    @Column(nullable = false, length = 40)
    public String kind = "";
    @Column(nullable = false, precision = 15, scale = 3)
    public BigDecimal amount = BigDecimal.ZERO;
    @Column(nullable = false)
    public LocalDate effectiveDate;
    @Column(nullable = false)
    public String reason = "";
    @Column(nullable = false)
    public String idempotencyKey = "";
    @Column(nullable = true)
    public Long reversesId;
}
