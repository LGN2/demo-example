package om.bayt.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "meter_reading")
public class MeterReading extends Row {
    @Column(nullable = false)
    public Long meterId;
    @Column(nullable = false)
    public LocalDate readingDate;
    @Column(nullable = false, precision = 15, scale = 3)
    public BigDecimal value = BigDecimal.ZERO;
    @Column(nullable = false, length = 40)
    public String kind = "";
    @Column(nullable = false)
    public String observation = "";
}
