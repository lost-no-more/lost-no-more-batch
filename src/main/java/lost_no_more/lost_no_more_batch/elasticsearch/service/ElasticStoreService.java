package lost_no_more.lost_no_more_batch.elasticsearch.service;

import co.elastic.clients.elasticsearch._types.GeoBounds;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FuzzyQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.GeoBoundingBoxQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.PrefixQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lost_no_more.lost_no_more_batch.elasticsearch.domain.LostItemDocument;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ElasticStoreService {

    private final ElasticsearchOperations elasticsearchOperations;

    @Transactional(readOnly = true)
    public SearchHits<LostItemDocument> searchLostItems(
            LocalDate dateStart,
            LocalDate dateEnd,
            Double topLeftLat,
            Double topLeftLon,
            Double bottomRightLat,
            Double bottomRightLon,
            String keyword,
            Long categoryId,
            String region,
            Integer size
    ) {

        List<Query> mustQueries = new ArrayList<>();
        List<Query> shouldQueries = new ArrayList<>();

        // 날짜 범위
        RangeQuery dateRangeQuery = new RangeQuery.Builder()
                .date(d -> d
                        .field("date")
                        .gte(dateStart.toString())
                        .lte(dateEnd.toString())
                )
                .build();
        mustQueries.add(dateRangeQuery._toQuery());

        // 키워드 검색
        boolean hasKeyword = (keyword != null && !keyword.isBlank());

        if (hasKeyword) {
            // 유사 검색
            FuzzyQuery fuzzyQuery = FuzzyQuery.of(f -> f
                    .field("name")
                    .value(keyword)
                    .fuzziness("AUTO")
            );
            shouldQueries.add(fuzzyQuery._toQuery());

            // 부분 검색
            PrefixQuery prefixQuery = PrefixQuery.of(p -> p
                    .field("name")
                    .value(keyword)
            );
            shouldQueries.add(prefixQuery._toQuery());
        }

        // 카테고리
        if (categoryId != null) {
            TermQuery termQuery = TermQuery.of(t -> t
                    .field("categoryId")
                    .value(categoryId)
            );
            mustQueries.add(termQuery._toQuery());
        }

        // 지역
        if (region != null && !region.isBlank()) {
            MatchQuery regionQuery = MatchQuery.of(m -> m
                    .field("region")
                    .query(region)
            );
            mustQueries.add(regionQuery._toQuery());
        }

        // 위치 필터
        if (topLeftLat != null && topLeftLon != null && bottomRightLat != null && bottomRightLon != null) {
            GeoBoundingBoxQuery geoQuery = GeoBoundingBoxQuery.of(g -> g
                    .field("location")
                    .boundingBox(
                            GeoBounds.of(outer -> outer.tlbr(b -> b
                                    .topLeft(GeoLocation.of(loc ->
                                            loc.text(topLeftLat + "," + topLeftLon)
                                    ))
                                    .bottomRight(GeoLocation.of(loc ->
                                            loc.text(bottomRightLat + "," + bottomRightLon)
                                    ))
                            ))
                    )
            );
            mustQueries.add(geoQuery._toQuery());
        }

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder()
                .must(mustQueries); // 필수 조건 추가

        if (hasKeyword) {
            boolQueryBuilder.should(shouldQueries);
            boolQueryBuilder.minimumShouldMatch("1");
        }

        Query boolQuery = boolQueryBuilder.build()._toQuery();

        List<SortOptions> sortOptions = List.of(
                SortOptions.of(s -> s.field(f -> f.field("date").order(SortOrder.Desc))),
                SortOptions.of(s -> s.field(f -> f.field("id").order(SortOrder.Desc)))
        );

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withSort(sortOptions)
                .withPageable(size != null ? PageRequest.of(0, size) : Pageable.unpaged())
                .build();

        return elasticsearchOperations.search(searchQuery, LostItemDocument.class);
    }

    @Transactional(readOnly = true)
    public SearchHits<LostItemDocument> searchLostItemsForSubscription(
            LocalDate dateStart, LocalDate dateEnd,
            String keyword, Long categoryId, String region, Integer size) {
        return searchLostItems(dateStart, dateEnd, null, null, null, null, keyword, categoryId, region, size);
    }
}
