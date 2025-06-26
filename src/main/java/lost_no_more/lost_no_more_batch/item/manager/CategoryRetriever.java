package lost_no_more.lost_no_more_batch.item.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lost_no_more.lost_no_more_batch.item.domain.Category;
import lost_no_more.lost_no_more_batch.item.repository.CategoryRepository;

@Component
@RequiredArgsConstructor
public class CategoryRetriever {

    private final CategoryRepository categoryRepository;

    public Map<String, Long> findCategoryIdFromDB(List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return new HashMap<>();
        }

        List<Category> categories = categoryRepository.findByNameIn(categoryNames);
        return categories.stream()
            .collect(Collectors.toMap(Category::getName, Category::getId));
    }
}
