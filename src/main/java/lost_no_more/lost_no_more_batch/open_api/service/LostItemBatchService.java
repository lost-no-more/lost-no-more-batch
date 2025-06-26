package lost_no_more.lost_no_more_batch.open_api.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lost_no_more.lost_no_more_batch.item.manager.CategoryCreator;
import lost_no_more.lost_no_more_batch.item.manager.CategoryRetriever;
import lost_no_more.lost_no_more_batch.item.manager.LocationRetriever;
import lost_no_more.lost_no_more_batch.item.manager.LostItemCreator;
import lost_no_more.lost_no_more_batch.item.service.KakaoApiService;
import lost_no_more.lost_no_more_batch.open_api.dto.LostItemDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class LostItemBatchService {

    private final CategoryCreator categoryCreator;
    private final CategoryRetriever categoryRetriever;
    private final LocationRetriever locationRetriever;
    private final LostItemCreator lostItemCreator;
    private final KakaoApiService kakaoApiService;

    @Transactional("dataTransactionManager")
    public void saveLostItems(List<LostItemDto> lostItemDtos) {
        log.info("✅ 배치 INSERT 시작 - 처리할 데이터: {}건", lostItemDtos.size());

        Map<String, Long> categoryIdMap = prepareCategoryData(lostItemDtos);

        Map<String, Long> locationIdMap = prepareLocationData(lostItemDtos);

        lostItemCreator.saveAll(lostItemDtos, categoryIdMap, locationIdMap);

        log.info("✅ 배치 INSERT 완료 - 총 {}건 저장", lostItemDtos.size());
    }

    private Map<String, Long> prepareCategoryData(List<LostItemDto> lostItemDtos) {
        Set<String> uniqueCategoryNames = extractUniqueCategoryNames(lostItemDtos);
        log.info("✅ 카테고리 처리 - 고유 카테고리 수: {}", uniqueCategoryNames.size());

        Map<String, Long> categoryIdMap = categoryRetriever.findCategoryIdFromDB(new ArrayList<>(uniqueCategoryNames));

        Set<String> newCategoryNames = new HashSet<>(uniqueCategoryNames);
        newCategoryNames.removeAll(categoryIdMap.keySet());

        if (!newCategoryNames.isEmpty()) {
            log.info("✅ 신규 카테고리 생성: {}개", newCategoryNames.size());

            categoryCreator.saveAll(new ArrayList<>(newCategoryNames));

            Map<String, Long> newCategoryIdMap = categoryRetriever.findCategoryIdFromDB(new ArrayList<>(newCategoryNames));
            categoryIdMap.putAll(newCategoryIdMap);
        }

        return categoryIdMap;
    }

    private Set<String> extractUniqueCategoryNames(List<LostItemDto> lostItemDtos) {
        Set<String> uniqueCategoryNames = new HashSet<>();
        for (LostItemDto dto : lostItemDtos) {
            String categoryName = dto.getCategoryName();
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                uniqueCategoryNames.add(categoryName.trim());
            }
        }
        return uniqueCategoryNames;
    }

    private Map<String, Long> prepareLocationData(List<LostItemDto> lostItemDtos) {
        Set<String> uniqueLocationNames = extractUniqueLocationNames(lostItemDtos);
        log.info("✅ 위치 처리 - 고유 위치 수: {}", uniqueLocationNames.size());

        Map<String, Long> locationIdMap = locationRetriever.findLocationIdMapFromDB(new ArrayList<>(uniqueLocationNames));

        Set<String> newLocationNames = new HashSet<>(uniqueLocationNames);
        newLocationNames.removeAll(locationIdMap.keySet());

        if (!newLocationNames.isEmpty()) {
            log.info("✅ 신규 위치 생성: {}개 (API 호출)", newLocationNames.size());

            kakaoApiService.findOrCreateLocations(new ArrayList<>(newLocationNames));

            Map<String, Long> newLocationIdMap = locationRetriever.findLocationIdMapFromDB(new ArrayList<>(newLocationNames));
            locationIdMap.putAll(newLocationIdMap);
        }

        return locationIdMap;
    }

    private Set<String> extractUniqueLocationNames(List<LostItemDto> lostItemDtos) {
        Set<String> uniqueLocationNames = new HashSet<>();
        for (LostItemDto dto : lostItemDtos) {
            String locationName = dto.getLocation();
            if (locationName != null && !locationName.trim().isEmpty()) {
                uniqueLocationNames.add(locationName.trim());
            }
        }
        return uniqueLocationNames;
    }
}