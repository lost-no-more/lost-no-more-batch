package lost_no_more.lost_no_more_batch.elasticsearch.writer;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lost_no_more.lost_no_more_batch.elasticsearch.domain.LostItemDocument;
import lost_no_more.lost_no_more_batch.elasticsearch.dto.ElasticLostItemDto;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticStoreWriter implements ItemWriter<ElasticLostItemDto> {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void write(Chunk<? extends ElasticLostItemDto> items) throws Exception {
        List<IndexQuery> queries = items.getItems().stream()
            .map(dto -> {
                LostItemDocument document = LostItemDocument.builder()
                    .id(dto.getLostItemId())
                    .name(dto.getLostItemName())
                    .date(dto.getLocalDateTime())
                    .categoryId(dto.getCategoryId())
                    .region(dto.getRegion())
                    .location(new GeoPoint(dto.getLatitude(), dto.getLongitude()))
                    .build();

                return new IndexQueryBuilder()
                    .withId(String.valueOf(dto.getLostItemId()))
                    .withObject(document)
                    .build();
            })
            .collect(Collectors.toList());

        if (!queries.isEmpty()) {
            log.info("💾 Bulk writing {} documents to Elasticsearch", queries.size());

            elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of("lost_item"));

            log.info("✅ Successfully bulk wrote {} documents", queries.size());
        }
    }
}