package lost_no_more.lost_no_more_batch.item.manager;

import lombok.RequiredArgsConstructor;
import lost_no_more.lost_no_more_batch.item.domain.Location;
import lost_no_more.lost_no_more_batch.item.repository.LocationRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationCreator {

    private final LocationRepository locationRepository;

    public void save(Location location) {
        locationRepository.save(location);
    }
}
