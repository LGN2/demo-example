package om.bayt.domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface LeaseRepository extends JpaRepository<Lease, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lease l where l.id = :id")
    Optional<Lease> lockById(Long id);
}
