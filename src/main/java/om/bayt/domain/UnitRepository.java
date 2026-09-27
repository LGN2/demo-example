package om.bayt.domain;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface UnitRepository extends JpaRepository<Unit, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from Unit u where u.id = :id")
  Optional<Unit> lockById(Long id);
}
