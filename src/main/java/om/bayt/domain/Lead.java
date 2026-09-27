package om.bayt.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "leasing_lead")
public class Lead extends Row {
    @Column(nullable = false)
    public Long buildingId;
    @Column(nullable = false)
    public Long unitId;
    @Column(nullable = false)
    public String name = "";
    @Column(nullable = false)
    public String phone = "";
    @Column(nullable = false, length = 40)
    public String status = "";
    @Column(nullable = true)
    public Instant viewingAt;
    @Column(nullable = true)
    public LocalDate followUpDate;
    @Column(nullable = false, columnDefinition = "TEXT")
    public String notes = "";
}
