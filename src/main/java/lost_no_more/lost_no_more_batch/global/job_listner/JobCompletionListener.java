package lost_no_more.lost_no_more_batch.global.job_listner;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobCompletionListener implements JobExecutionListener {

    private final JobRepository jobRepository;
    private static final Logger log = LoggerFactory.getLogger(JobCompletionListener.class);
    private Instant startTime;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = Instant.now();
        log.info("Batch Job [{}] started at {}", jobExecution.getJobInstance().getJobName(), startTime);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);
        log.info("Batch Job [{}] finished at {} | Duration: {} seconds",
                jobExecution.getJobInstance().getJobName(), endTime, duration.toSeconds());

        boolean hasFailedStep = jobExecution.getStepExecutions().stream()
                .anyMatch(step -> step.getExitStatus().equals(ExitStatus.FAILED));

        if (hasFailedStep || jobExecution.getStatus().isUnsuccessful()) {
            jobExecution.setExitStatus(ExitStatus.FAILED);
            jobExecution.setStatus(org.springframework.batch.core.BatchStatus.FAILED);
        } else {
            jobExecution.setExitStatus(ExitStatus.COMPLETED);
            jobExecution.setStatus(org.springframework.batch.core.BatchStatus.COMPLETED);
        }

        jobRepository.update(jobExecution);
        System.exit(jobExecution.getExitStatus().equals(ExitStatus.FAILED) ? 1 : 0);
    }
}