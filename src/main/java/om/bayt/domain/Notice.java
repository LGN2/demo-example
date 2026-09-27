package om.bayt.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "notice")
public class Notice extends Row {
    @Column(nullable = false)
    public Long buildingId;
    @Column(nullable = false)
    public String titleAr = "";
    @Column(nullable = false)
    public String titleEn = "";
    @Column(nullable = false, columnDefinition = "TEXT")
    public String bodyAr = "";
    @Column(nullable = false, columnDefinition = "TEXT")
    public String bodyEn = "";
}
