package lost_no_more.lost_no_more_batch.item.repository;

import lost_no_more.lost_no_more_batch.item.domain.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {

}
