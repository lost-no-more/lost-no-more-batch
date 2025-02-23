package lost_no_more.lost_no_more_batch.item.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lost_no_more.lost_no_more_batch.item.domain.Location;
import lost_no_more.lost_no_more_batch.item.manager.LocationCreator;
import lost_no_more.lost_no_more_batch.item.manager.LocationRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    @Value("${kakao.map.secret-key}")
    private String kakaoApiKey;

    @Value("${kakao.map.url}")
    private String kakaoApiUrl;

    private final LocationRetriever locationRetriever;
    private final LocationCreator locationCreator;

    public Location findOrCreateLocation(String locationName) {

        Location location;
        location = locationRetriever.findByName(locationName);

        if (location == null) {
            location = saveLocation(locationName);
        }

        return location;
    }

    private Location saveLocation(String locationName) {
        Location location = callKakoMapApi(locationName);
        return saveLocationIfNotNull(location, locationName);
    }

    private Location callKakoMapApi(String locationName) {
        try {
            String apiUrl = kakaoApiUrl + URLEncoder.encode(locationName, "UTF-8");

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "KakaoAK " + kakaoApiKey);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }
            br.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBuilder.toString());
            JsonNode documents = root.get("documents");

            if (documents != null && documents.size() > 0) {
                JsonNode firstResult = documents.get(0);

                String region = firstResult.get("address_name").asText().split(" ")[0];
                double longitude = firstResult.get("x").asDouble();
                double latitude = firstResult.get("y").asDouble();
                String placeName = firstResult.get("place_name").asText();

                log.info("Kakao API 응답 - 장소명: {}, 지역: {}, 경도: {}, 위도: {}", placeName, region, longitude, latitude);

                return new Location(placeName, latitude, longitude, region);
            } else {
                log.warn("Kakao API에서 결과를 찾을 수 없습니다: {}", locationName);
            }

        } catch (Exception e) {
            log.error("Kakao API 호출 중 오류 발생: {}", e.getMessage());
        }
        return null;
    }

    private Location saveLocationIfNotNull(Location location, String locationName) {
        if (location != null) {
            if (locationRetriever.existsByName(locationName)) {
                return location;
            }
            locationCreator.save(location);
            return location;
        } else {
            log.warn("Kakao API에서 위치를 찾을 수 없습니다: {}", locationName);
            return null;
        }
    }
}