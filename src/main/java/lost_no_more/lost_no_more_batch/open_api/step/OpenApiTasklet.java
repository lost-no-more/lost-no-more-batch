package lost_no_more.lost_no_more_batch.open_api.step;

import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lost_no_more.lost_no_more_batch.global.exception.BusinessException;
import lost_no_more.lost_no_more_batch.global.exception.code.BusinessErrorCode;
import lost_no_more.lost_no_more_batch.item.service.LostItemService;
import lost_no_more.lost_no_more_batch.open_api.dto.LostItemDto;
import lost_no_more.lost_no_more_batch.open_api.parameters.OpenApiJobParameters;
import lost_no_more.lost_no_more_batch.open_api.service.OpenApiService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Slf4j
@StepScope
@Component
@RequiredArgsConstructor
public class OpenApiTasklet implements Tasklet {

    private final OpenApiService openApiService;
    private final LostItemService lostItemService;
    private final OpenApiJobParameters openApiJobParameters;

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String startDate = openApiJobParameters.getStartDate();
        String endDate = openApiJobParameters.getEndDate();
        Integer pageNo = openApiJobParameters.getCurrentPage(chunkContext);
        Integer numOfRows = openApiJobParameters.getNumOfRows();

        log.info("현재 page: {}", pageNo);

        try {
            String apiResponse = openApiService.callApi(startDate, endDate, pageNo, numOfRows);

            if (isError(apiResponse)) {
                contribution.setExitStatus(ExitStatus.FAILED);
                throw new BusinessException(BusinessErrorCode.OPEN_API_ANSWER_ERROR);
            }

            if (isItemsEmpty(apiResponse)) {
                log.info("모든 아이템 Step 종료. pageNo: {}", pageNo);
                contribution.setExitStatus(ExitStatus.COMPLETED);
                return RepeatStatus.FINISHED;
            }

            List<LostItemDto> lostItemDtos = lostItemService.parseXmlToDto(apiResponse);
            lostItemService.saveLostItems(lostItemDtos);

        } catch (BusinessException e) {
            log.error("API 호출 또는 데이터 저장 중 오류 발생, pageNo: {}, error 내용: {}", pageNo, e.getErrorCode().getMessage());
            contribution.setExitStatus(ExitStatus.FAILED);
            throw e;
        }

        incrementPageNo(chunkContext, pageNo);

        contribution.setExitStatus(ExitStatus.EXECUTING);
        return RepeatStatus.CONTINUABLE;
    }

    private boolean isError(String apiResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new java.io.ByteArrayInputStream(apiResponse.getBytes()));

        String resultCode = document.getElementsByTagName("resultCode").item(0).getTextContent();
        String resultMsg = document.getElementsByTagName("resultMsg").item(0).getTextContent();

        log.info("resultCode: {}", resultCode);
        log.info("resultMsg: {}", resultMsg);

        return !"00".equals(resultCode);
    }

    private boolean isItemsEmpty(String apiResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new java.io.ByteArrayInputStream(apiResponse.getBytes()));

        NodeList items = document.getElementsByTagName("item");
        return items.getLength() == 0;
    }

    private void incrementPageNo(ChunkContext chunkContext, Integer currentPage) {
        int nextPage = currentPage + 1;
        chunkContext.getStepContext().getStepExecution()
                .getExecutionContext().putInt("PAGE_NO", nextPage);
    }
}