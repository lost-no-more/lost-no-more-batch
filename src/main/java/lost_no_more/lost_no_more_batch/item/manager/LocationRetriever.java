package lost_no_more.lost_no_more_batch.item.manager;

import lombok.RequiredArgsConstructor;
import lost_no_more.lost_no_more_batch.item.domain.Category;
import lost_no_more.lost_no_more_batch.item.domain.Location;
import lost_no_more.lost_no_more_batch.item.repository.LocationRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationRetriever {

    private final LocationRepository locationRepository;

    public Location findByName(final String name) {
        return locationRepository.findByName(name)
                .orElse(null);
    }

    public boolean existsByName(final String name) {
        return locationRepository.existsByName(name);
    }
}
