package lost_no_more.lost_no_more_batch.item.manager;

import lombok.RequiredArgsConstructor;
import lost_no_more.lost_no_more_batch.item.domain.Category;
import lost_no_more.lost_no_more_batch.item.repository.CategoryRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryRetriever {

    private final CategoryRepository categoryRepository;

    public Category findById(final String categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElse(null);
    }
}
