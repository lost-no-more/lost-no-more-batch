package lost_no_more.lost_no_more_batch.elastic_item.parameters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@StepScope
@Component
public class ElasticStoreJobParameters {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private LocalDate startDate;
    private LocalDate endDate;

    @Value("#{jobParameters['START_DATE']}")
    public void setStartDate(String startDate) {
        this.startDate = LocalDate.parse(startDate, formatter);
    }

    @Value("#{jobParameters['END_DATE']}")
    public void setEndDate(String endDate) {
        this.endDate = LocalDate.parse(endDate, formatter);
    }
}