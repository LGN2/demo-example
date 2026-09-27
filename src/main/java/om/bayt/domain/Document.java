package om.bayt.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "document")
public class Document extends Row {
    @Column(nullable = false)
    public Long buildingId;
    @Column(nullable = true)
    public Long unitId;
    @Column(nullable = true)
    public Long tenantId;
    @Column(nullable = true)
    public Long maintenanceId;
    @Column(nullable = true)
    public Long readingId;
    @Column(nullable = false, length = 40)
    public String kind = "";
    @Column(nullable = false)
    public String filename = "";
    @Column(nullable = false)
    @JsonIgnore
    public String storageKey = "";
    @Column(nullable = false)
    public String contentType = "";
    @Column(nullable = false)
    public Long sizeBytes;
    @Column(nullable = false, length = 40)
    public String scanStatus = "";
    @Column(nullable = true)
    public LocalDate expiryDate;
}
