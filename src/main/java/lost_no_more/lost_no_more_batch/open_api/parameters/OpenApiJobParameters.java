package lost_no_more.lost_no_more_batch.open_api.parameters;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@StepScope
@Component
public class OpenApiJobParameters {

    @Value("#{jobParameters['START_DATE']}")
    private String startDate;

    @Value("#{jobParameters['END_DATE']}")
    private String endDate;

    @Value("#{jobParameters['PAGE_NO']}")
    private Long pageNo;

    @Value("#{jobParameters['NUM_OF_ROWS']}")
    private Long numOfRows;
}
