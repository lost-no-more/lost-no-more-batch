package lost_no_more.lost_no_more_batch.item.manager;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryCreator {

	@Qualifier("dataJdbcTemplate")
	private final JdbcTemplate jdbcTemplate;

	public void saveAll(List<String> categoryNames) {
		if (categoryNames == null || categoryNames.isEmpty()) {
			return;
		}

		String sql = "INSERT INTO category (category_name) VALUES (?)";
		List<Object[]> batchArgs = new ArrayList<>();

		for (String categoryName : categoryNames) {
			Object[] args = new Object[]{categoryName};
			batchArgs.add(args);
		}

		try {
			int batchSize = 1000;
			for (int i = 0; i < batchArgs.size(); i += batchSize) {
				int end = Math.min(i + batchSize, batchArgs.size());
				List<Object[]> currentBatch = batchArgs.subList(i, end);

				int[] updateCounts = jdbcTemplate.batchUpdate(sql, currentBatch);
				log.info("✅ 카테고리 배치 {}~{} 처리 완료: {}건", i + 1, end, updateCounts.length);
			}
			log.info("💾 카테고리 Bulk INSERT 완료: {} 개 카테고리 저장", categoryNames.size());
		} catch (Exception e) {
			log.error("❌ 카테고리 Bulk insert 실패: {}", e.getMessage());
			throw e;
		}
	}
}
