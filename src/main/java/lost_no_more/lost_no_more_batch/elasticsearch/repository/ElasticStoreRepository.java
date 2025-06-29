package lost_no_more.lost_no_more_batch.elasticsearch.repository;

import lost_no_more.lost_no_more_batch.elasticsearch.domain.LostItemDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticStoreRepository extends ElasticsearchRepository<LostItemDocument, Long> {
}
