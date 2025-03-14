package lost_no_more.lost_no_more_batch.elastic_item.writer;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lost_no_more.lost_no_more_batch.elastic_item.domain.LostItemDocument;
import lost_no_more.lost_no_more_batch.elastic_item.dto.ElasticLostItemDto;
import lost_no_more.lost_no_more_batch.elastic_item.repository.ElasticStoreRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElasticStoreWriter implements ItemWriter<ElasticLostItemDto> {

    private final ElasticStoreRepository lostItemDocumentRepository;

    @Override
    public void write(Chunk<? extends ElasticLostItemDto> items) throws Exception {
        List<LostItemDocument> documents = items.getItems().stream()
                .map(dto -> LostItemDocument.builder()
                        .id(dto.getLostItemId())
                        .name(dto.getLostItemName())
                        .date(dto.getLocalDateTime())
                        .categoryId(dto.getCategoryId())
                        .region(dto.getRegion())
                        .location(new GeoPoint(dto.getLatitude(), dto.getLongitude()))
                        .build())
                .collect(Collectors.toList());
        if (!documents.isEmpty()) {
            lostItemDocumentRepository.saveAll(documents);
        }
    }
}