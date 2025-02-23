package lost_no_more.lost_no_more_batch.item.repository;

import java.util.Optional;
import lost_no_more.lost_no_more_batch.item.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByName(String name);

    boolean existsByName(String name);
}
