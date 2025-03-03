package lost_no_more.lost_no_more_batch.open_api.parameters;

import lombok.Getter;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@StepScope
@Component
public class OpenApiJobParameters {

    private String startDate;
    private String endDate;
    private Integer pageNo;
    private Integer numOfRows;

    @Value("#{jobParameters['START_DATE']}")
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @Value("#{jobParameters['END_DATE']}")
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Value("#{jobParameters['PAGE_NO']}")
    public void setPageNo(String pageNo) {
        this.pageNo = Integer.valueOf(pageNo);
    }

    @Value("#{jobParameters['NUM_OF_ROWS']}")
    public void setNumOfRows(String numOfRows) {
        this.numOfRows = Integer.valueOf(numOfRows);
    }

    public Integer getCurrentPage(ChunkContext chunkContext) {
        Integer currentPage = (Integer) chunkContext.getStepContext()
                .getStepExecution()
                .getExecutionContext()
                .get("PAGE_NO");

        if (currentPage == null) {
            currentPage = this.pageNo;
        }

        return currentPage;
    }
}
