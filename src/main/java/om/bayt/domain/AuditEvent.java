package om.bayt.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "audit_event")
public class AuditEvent extends Row {
    @Column(nullable = true)
    public Long buildingId;
    @Column(nullable = false, length = 40)
    public String resourceType = "";
    @Column(nullable = false)
    public Long resourceId;
    @Column(nullable = false)
    public Long actorId;
    @Column(nullable = false, length = 40)
    public String action = "";
    @Column(nullable = false, columnDefinition = "TEXT")
    public String note = "";
}
