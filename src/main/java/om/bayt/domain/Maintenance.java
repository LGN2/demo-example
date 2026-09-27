package om.bayt.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "maintenance")
public class Maintenance extends Row {
    @Transient public boolean seasonalPriority;
    @Column(nullable = false)
    public Long buildingId;
    @Column(nullable = false)
    public Long unitId;
    @Column(nullable = true)
    public Long tenantId;
    @Column(nullable = false, columnDefinition = "TEXT")
    public String description = "";
    @Column(nullable = false, length = 40)
    public String category = "";
    @Column(nullable = false)
    public boolean urgent;
    @Column(nullable = false, length = 40)
    public String status = "";
    @Column(nullable = true)
    public Long assignedTo;
    @Column(nullable = false)
    public String approvedSummary = "";
    @Column(nullable = false)
    public String proposedSummary = "";
    @Column(nullable = false, length = 40)
    public String proposedCategory = "";
    @Column(nullable = false, length = 40)
    public String aiStatus = "";
    @Column(nullable = true)
    public Instant resolvedAt;
    @Column(nullable = true)
    public Instant closedAt;
}
