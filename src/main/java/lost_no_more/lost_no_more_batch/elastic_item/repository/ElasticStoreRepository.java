package lost_no_more.lost_no_more_batch.elastic_item.repository;

import lost_no_more.lost_no_more_batch.elastic_item.domain.LostItemDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticStoreRepository extends ElasticsearchRepository<LostItemDocument, Long> {
}
