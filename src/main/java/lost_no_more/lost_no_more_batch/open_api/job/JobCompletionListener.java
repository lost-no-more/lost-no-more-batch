package lost_no_more.lost_no_more_batch.open_api.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobCompletionListener implements JobExecutionListener {

    private final JobRepository jobRepository;

    @Override
    public void afterJob(JobExecution jobExecution) {
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