package lost_no_more.lost_no_more_batch.item.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lost_no_more.lost_no_more_batch.item.domain.Location;
import lost_no_more.lost_no_more_batch.item.manager.LocationCreator;
import lost_no_more.lost_no_more_batch.item.manager.LocationRetriever;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiService {

    @Value("${kakao.map.secret-key}")
    private String kakaoApiKey;

    @Value("${kakao.map.url}")
    private String kakaoApiUrl;

    private final LocationRetriever locationRetriever;
    private final LocationCreator locationCreator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Location> locationCache = new HashMap<>();

    public Map<String, Location> findOrCreateLocations(List<String> locationNames) {
        Map<String, Location> result = new HashMap<>();

        Set<String> uncachedNames = new HashSet<>();
        for (String locationName : locationNames) {
            if (locationName == null || locationName.trim().isEmpty()) {
                continue;
            }
            Location cachedLocation = locationCache.get(locationName);
            if (cachedLocation != null) {
                result.put(locationName, cachedLocation);
                log.debug("캐시에서 위치 조회: {}", locationName);
            } else {
                uncachedNames.add(locationName);
            }
        }

        if (!uncachedNames.isEmpty()) {
            Map<String, Location> dbLocations = locationRetriever.findByNames(new ArrayList<>(uncachedNames));
            result.putAll(dbLocations);
            locationCache.putAll(dbLocations);

            Set<String> notFoundInDb = new HashSet<>(uncachedNames);
            notFoundInDb.removeAll(dbLocations.keySet());

            if (!notFoundInDb.isEmpty()) {
                createAndSaveNewLocations(notFoundInDb, result);
            }
        }

        return result;
    }

    private void createAndSaveNewLocations(Set<String> locationNames, Map<String, Location> result) {
        List<Location> newLocations = new ArrayList<>();

        for (String locationName : locationNames) {
            try {
                Location location = callKakaoMapApi(locationName);
                if (location != null) {
                    newLocations.add(location);
                }
            } catch (Exception e) {
                log.error("❌ 위치 생성 중 오류: {}, error: {}", locationName, e.getMessage());
            }
        }

        if (!newLocations.isEmpty()) {
            try {
                locationCreator.saveAll(newLocations);

                List<String> savedLocationNames = newLocations.stream()
                    .map(Location::getName)
                    .collect(Collectors.toList());

                Map<String, Location> savedLocations = locationRetriever.findByNames(savedLocationNames);

                log.info("✅ Bulk insert 완료: {} 개 위치 저장", savedLocations.size());

                for (Map.Entry<String, Location> entry : savedLocations.entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                    locationCache.put(entry.getKey(), entry.getValue());
                }

            } catch (Exception e) {
                log.error("❌ Bulk insert 실패: {}", e.getMessage());
            }
        }
    }

    private Location callKakaoMapApi(String locationName) {
        HttpURLConnection conn = null;
        BufferedReader br = null;

        try {
            String apiUrl = kakaoApiUrl + URLEncoder.encode(locationName, StandardCharsets.UTF_8);
            log.debug("🌐 Kakao API 호출: {}", locationName);

            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "KakaoAK " + kakaoApiKey);
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.warn("❌ Kakao API 호출 실패: {} - HTTP {}", locationName, responseCode);
                return null;
            }

            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder responseBuilder = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }

            return parseKakaoResponse(responseBuilder.toString(), locationName);

        } catch (Exception e) {
            log.error("❌ Kakao API 호출 중 오류: {} - {}", locationName, e.getMessage());
            return null;
        } finally {
            if (br != null) {
                try { br.close(); } catch (Exception e) { /* ignore */ }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private Location parseKakaoResponse(String response, String locationName) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode documents = root.get("documents");

            if (documents != null && documents.size() > 0) {
                JsonNode firstResult = documents.get(0);

                String addressName = firstResult.get("address_name").asText();
                String region = addressName.split(" ")[0];
                double longitude = firstResult.get("x").asDouble();
                double latitude = firstResult.get("y").asDouble();
                String placeName = firstResult.get("place_name").asText();

                log.info("✅ Kakao API 응답 - 장소: {}, 지역: {}, 좌표: ({}, {})",
                    placeName, region, longitude, latitude);

                return Location.builder()
                    .name(locationName)
                    .latitude(latitude)
                    .longitude(longitude)
                    .region(region)
                    .build();
            } else {
                log.warn("⚠️ Kakao API에서 결과를 찾을 수 없습니다: {}", locationName);
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Kakao API 응답 파싱 오류: {} - {}", locationName, e.getMessage());
            return null;
        }
    }
}