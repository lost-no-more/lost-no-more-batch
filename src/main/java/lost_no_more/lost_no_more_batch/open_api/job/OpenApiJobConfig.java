package lost_no_more.lost_no_more_batch.open_api.job;

import lombok.RequiredArgsConstructor;
import lost_no_more.lost_no_more_batch.open_api.step.OpenApiTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class OpenApiJobConfig {

    private final OpenApiTasklet openApiTasklet;
    private final JobRepository jobRepository;
    private final JobCompletionListener jobCompletionListener;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job openApiJob() {
        return new JobBuilder("openApiJob", jobRepository)
                .listener(jobCompletionListener)
                .start(openApiStep())
                .build();
    }

    @Bean
    public Step openApiStep() {
        return new StepBuilder("openApiStep", jobRepository)
                .tasklet(openApiTasklet, transactionManager)
                .build();
    }
}