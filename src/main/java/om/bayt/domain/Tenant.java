package om.bayt.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "tenant")
public class Tenant extends Row {
    @Column(nullable = false)
    public Long buildingId;
    @Column(nullable = true)
    public Long accountId;
    @Column(nullable = false)
    public String name = "";
    @Column(nullable = false, length = 40)
    public String kind = "";
    @Column(nullable = false)
    public String email = "";
    @Column(nullable = false)
    public String phone = "";
    @Column(nullable = false)
    public String emergencyContact = "";
}
