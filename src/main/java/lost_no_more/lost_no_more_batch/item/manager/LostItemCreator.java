package lost_no_more.lost_no_more_batch.item.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lost_no_more.lost_no_more_batch.open_api.dto.LostItemDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class LostItemCreator {

	@Qualifier("dataJdbcTemplate")
	private final JdbcTemplate jdbcTemplate;

	public void saveAll(List<LostItemDto> lostItemDtos,
		Map<String, Long> categoryIdMap,
		Map<String, Long> locationIdMap) {

		String sql = """
                INSERT INTO lost_item (category_id, location_id, atc_id, color, image, name, date, created_date, modified_date) 
                VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE 
                    category_id = VALUES(category_id),
                    location_id = VALUES(location_id),
                    color = VALUES(color),
                    image = VALUES(image),
                    name = VALUES(name),
                    date = VALUES(date),
                    modified_date = NOW()
                """;

		List<Object[]> batchArgs = new ArrayList<>();

		for (LostItemDto dto : lostItemDtos) {
			Long categoryId = categoryIdMap.get(dto.getCategoryName());
			Long locationId = locationIdMap.get(dto.getLocation());

			if (categoryId == null) {
				log.warn("⚠️ 카테고리 ID를 찾을 수 없음: {}", dto.getCategoryName());
				continue;
			}

			if (locationId == null) {
				log.warn("⚠️ 위치 ID를 찾을 수 없음: {}", dto.getLocation());
				continue;
			}

			Object[] args = new Object[]{
				categoryId,
				locationId,
				dto.getAtcId(),
				dto.getColor(),
				dto.getImage(),
				dto.getName(),
				dto.getDate()
			};

			batchArgs.add(args);
		}

		if (batchArgs.isEmpty()) {
			log.warn("⚠️ 배치 INSERT할 유효한 데이터가 없습니다.");
			return;
		}

		log.info("✅ JDBC Batch INSERT 실행 - 유효 데이터: {}건", batchArgs.size());

		int batchSize = 1000;
		for (int i = 0; i < batchArgs.size(); i += batchSize) {
			int end = Math.min(i + batchSize, batchArgs.size());
			List<Object[]> currentBatch = batchArgs.subList(i, end);

			int[] updateCounts = jdbcTemplate.batchUpdate(sql, currentBatch);
			log.info("✅ 배치 {}~{} 처리 완료: {}건", i + 1, end, updateCounts.length);
		}
	}
}