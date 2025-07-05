package lost_no_more.lost_no_more_batch.elasticsearch.domain;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
@Document(indexName = "lost_item")
public class LostItemDocument {

    @Id
    private Long id;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "standard"),
        otherFields = {
            @InnerField(suffix = "nori", type = FieldType.Text, analyzer = "nori_analyzer"),
            @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer")
        }
    )
    private String name;

    @Field(type = FieldType.Date)
    private LocalDate date;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    private String region;

    @GeoPointField
    private GeoPoint location;

    public LostItemDocument() {
    }

    @Builder
    public LostItemDocument(
        Long id,
        String name,
        LocalDate date,
        Long categoryId,
        String region,
        GeoPoint location
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.categoryId = categoryId;
        this.region = region;
        this.location = location;
    }

}