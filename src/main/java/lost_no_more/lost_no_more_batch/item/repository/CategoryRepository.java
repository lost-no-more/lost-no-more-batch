package lost_no_more.lost_no_more_batch.item.repository;

import java.util.Optional;
import lost_no_more.lost_no_more_batch.item.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String categoryName);
}
