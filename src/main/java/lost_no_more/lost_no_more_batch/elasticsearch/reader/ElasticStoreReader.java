package lost_no_more.lost_no_more_batch.elasticsearch.reader;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lost_no_more.lost_no_more_batch.elasticsearch.dto.ElasticLostItemDto;
import lost_no_more.lost_no_more_batch.elasticsearch.parameters.ElasticStoreJobParameters;
import lost_no_more.lost_no_more_batch.global.reader.QuerydslNoOffsetPagingItemReader;
import lost_no_more.lost_no_more_batch.global.reader.QuerydslNoOffsetPagingItemReaderBuilder;
import lost_no_more.lost_no_more_batch.global.reader.expression.Expression;
import lost_no_more.lost_no_more_batch.global.reader.options.QuerydslNoOffsetNumberOptions;
import lost_no_more.lost_no_more_batch.global.reader.options.QuerydslNoOffsetOptions;
import lost_no_more.lost_no_more_batch.item.domain.QCategory;
import lost_no_more.lost_no_more_batch.item.domain.QLocation;
import lost_no_more.lost_no_more_batch.item.domain.QLostItem;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ElasticStoreReader {

    private final EntityManagerFactory emf;

    private final QLostItem lostItem = QLostItem.lostItem;
    private final QLocation location = QLocation.location;
    private final QCategory category = QCategory.category;
    private final ElasticStoreJobParameters elasticStoreJobParameters;

    @Bean
    @StepScope
    public QuerydslNoOffsetPagingItemReader<ElasticLostItemDto> querydslNoOffsetPagingItemReader() {
        QuerydslNoOffsetOptions<ElasticLostItemDto> options = QuerydslNoOffsetNumberOptions.of(lostItem.id,
                Expression.ASC, "lostItemId");

        return QuerydslNoOffsetPagingItemReaderBuilder.<ElasticLostItemDto>builder()
                .entityManagerFactory(emf)
                .pageSize(1000)
                .options(options)
                .queryFunction(queryFactory -> queryFactory
                        .select(createConstructorExpression())
                        .from(lostItem)
                        .innerJoin(location).on(lostItem.location.id.eq(location.id))
                        .innerJoin(category).on(lostItem.category.id.eq(category.id))
                        .where(lostItem.date.between(elasticStoreJobParameters.getStartDate(),
                                elasticStoreJobParameters.getEndDate()))
                )
                .idSelectQuery(queryFactory -> queryFactory.select(createConstructorExpression()).from(lostItem))
                .build();
    }

    private ConstructorExpression<ElasticLostItemDto> createConstructorExpression() {
        return Projections.constructor(
                ElasticLostItemDto.class,
                lostItem.id,
                lostItem.date,
                lostItem.name,
                category.id,
                location.region,
                location.latitude,
                location.longitude
        );
    }
}
