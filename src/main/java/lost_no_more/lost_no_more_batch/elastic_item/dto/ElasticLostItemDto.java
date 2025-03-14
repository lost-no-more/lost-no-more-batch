package lost_no_more.lost_no_more_batch.elastic_item.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ElasticLostItemDto {

    private Long lostItemId;
    private LocalDate localDateTime;
    private String lostItemName;
    private Long categoryId;
    private String region;
    private Double latitude;
    private Double longitude;
}