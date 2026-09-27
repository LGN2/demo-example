package om.bayt.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "building_access")
public class BuildingAccess extends Row {
    @Column(nullable = false)
    public Long buildingId;
    @Column(nullable = false)
    public Long userId;
    @Column(nullable = false)
    public boolean canWrite;
}
