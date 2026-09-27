package om.bayt.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "guard_check_in")
public class GuardCheckIn extends Row {
    @Column(nullable = false)
    public Long buildingId;
    @Column(nullable = false)
    public Long userId;
    @Column(nullable = false)
    public String note = "";
}
