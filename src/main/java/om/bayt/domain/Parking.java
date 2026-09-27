package om.bayt.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "parking")
public class Parking extends Row {
    @Column(nullable = false)
    public Long buildingId;
    @Column(nullable = false)
    public Long unitId;
    @Column(nullable = false)
    public String space = "";
    @Column(nullable = false)
    public String vehicle = "";
}
