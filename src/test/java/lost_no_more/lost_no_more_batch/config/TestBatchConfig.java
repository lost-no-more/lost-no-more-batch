package lost_no_more.lost_no_more_batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class TestBatchConfig {

    @Bean
    public JobLauncherTestUtils openApiJobLauncherTestUtils(Job openApiJob, JobLauncher jobLauncher) {
        JobLauncherTestUtils jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJob(openApiJob);
        return jobLauncherTestUtils;
    }
}