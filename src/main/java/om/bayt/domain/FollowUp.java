package om.bayt.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "follow_up")
public class FollowUp extends Row {
    @Column(nullable = false)
    public Long leaseId;
    @Column(nullable = false)
    public String note = "";
    @Column(nullable = true)
    public LocalDate nextDate;
}
