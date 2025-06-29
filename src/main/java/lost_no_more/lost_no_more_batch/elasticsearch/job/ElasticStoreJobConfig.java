package lost_no_more.lost_no_more_batch.elasticsearch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lost_no_more.lost_no_more_batch.elasticsearch.dto.ElasticLostItemDto;
import lost_no_more.lost_no_more_batch.elasticsearch.reader.ElasticStoreReader;
import lost_no_more.lost_no_more_batch.elasticsearch.writer.ElasticStoreWriter;

@Configuration
@RequiredArgsConstructor
public class ElasticStoreJobConfig {

    private final JobRepository jobRepository;
    private final ElasticStoreReader reader;
    private final PlatformTransactionManager dataTransactionManager;
    private final ElasticStoreWriter writer;

    @Bean
    public Job dailyElasticStoreJob() {
        return new JobBuilder("dailyElasticStoreJob", jobRepository)
                .start(dailyElasticStoreStep())
                .on("FAILED")
                .stopAndRestart(dailyElasticStoreStep())
                .on("*")
                .end()
                .end()
                .build();
    }

    @Bean
    public Step dailyElasticStoreStep() {
        return new StepBuilder("dailyElasticStoreStep", jobRepository)
                .<ElasticLostItemDto, ElasticLostItemDto>chunk(1000, dataTransactionManager)
                .reader(reader.querydslNoOffsetPagingItemReader())
                .writer(writer)
                .build();
    }

}