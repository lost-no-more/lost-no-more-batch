package lost_no_more.lost_no_more_batch.item.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lost_no_more.lost_no_more_batch.item.domain.Location;
import lost_no_more.lost_no_more_batch.item.repository.LocationRepository;

@Component
@RequiredArgsConstructor
public class LocationRetriever {

    private final LocationRepository locationRepository;

    public Map<String, Location> findByNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return new HashMap<>();
        }

        List<Location> locations = locationRepository.findByNameIn(names);

        Map<String, Location> locationMap = new HashMap<>();
        for (Location location : locations) {
            locationMap.put(location.getName(), location);
        }

        return locationMap;
    }

    public Map<String, Long> findLocationIdMapFromDB(List<String> locationNames) {
        if (locationNames == null || locationNames.isEmpty()) {
            return new HashMap<>();
        }

        List<Location> locations = locationRepository.findByNameIn(locationNames);
        return locations.stream()
            .collect(Collectors.toMap(Location::getName, Location::getId));
    }
}