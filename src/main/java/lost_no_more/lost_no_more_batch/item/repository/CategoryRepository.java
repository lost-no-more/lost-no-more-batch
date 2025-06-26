package lost_no_more.lost_no_more_batch.item.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import lost_no_more.lost_no_more_batch.item.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByNameIn(List<String> categoryNames);
}