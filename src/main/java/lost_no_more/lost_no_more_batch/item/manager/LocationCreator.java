package lost_no_more.lost_no_more_batch.item.manager;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lost_no_more.lost_no_more_batch.item.domain.Location;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationCreator {

    @Qualifier("dataJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    public void saveAll(List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            return;
        }

        try {
            String sql = """
            INSERT INTO location (name, latitude, longitude, region, created_date, modified_date) 
            VALUES (?, ?, ?, ?, NOW(), NOW())
            """;

            int batchSize = 1000;
            List<Object[]> currentBatch = new ArrayList<>();
            int processedCount = 0;

            for (Location location : locations) {
                Object[] args = new Object[]{
                    location.getName(),
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getRegion()
                };
                currentBatch.add(args);

                if (currentBatch.size() == batchSize || processedCount == locations.size() - 1) {
                    jdbcTemplate.batchUpdate(sql, currentBatch);
                    log.info("✅ 위치 배치 처리 완료: {}건", currentBatch.size());
                    currentBatch.clear();
                }

                processedCount++;
            }

            log.info("💾 위치 Bulk INSERT 완료 - 총 위치 데이터: {}건", locations.size());

        } catch (Exception e) {
            log.error("❌ JDBC bulk insert 실패: {}", e.getMessage());
            throw e;
        }
    }
}