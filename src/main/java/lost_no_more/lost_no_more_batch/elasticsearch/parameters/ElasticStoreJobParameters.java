package lost_no_more.lost_no_more_batch.elasticsearch.parameters;

import java.time.LocalDate;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@StepScope
@Component
public class ElasticStoreJobParameters {

    @Value("#{jobParameters['START_DATE']}")
    private LocalDate startDate;

    @Value("#{jobParameters['END_DATE']}")
    private LocalDate endDate;
}