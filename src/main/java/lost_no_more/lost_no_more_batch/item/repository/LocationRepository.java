package lost_no_more.lost_no_more_batch.item.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import lost_no_more.lost_no_more_batch.item.domain.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByNameIn(List<String> names);
}