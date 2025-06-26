package lost_no_more.lost_no_more_batch.open_api.step;

import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lost_no_more.lost_no_more_batch.global.exception.BusinessException;
import lost_no_more.lost_no_more_batch.global.exception.code.BusinessErrorCode;
import lost_no_more.lost_no_more_batch.open_api.service.LostItemParseService;
import lost_no_more.lost_no_more_batch.open_api.dto.LostItemDto;
import lost_no_more.lost_no_more_batch.open_api.parameters.OpenApiJobParameters;
import lost_no_more.lost_no_more_batch.open_api.service.LostItemBatchService;
import lost_no_more.lost_no_more_batch.open_api.service.OpenApiService;
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
	private final LostItemParseService lostItemParseService;
	private final LostItemBatchService lostItemBatchService;
	private final OpenApiJobParameters openApiJobParameters;

	@Override
	@Transactional
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		String startDate = openApiJobParameters.getStartDate();
		String endDate = openApiJobParameters.getEndDate();
		Long startPageNo = openApiJobParameters.getPageNo();
		Long numOfRows = openApiJobParameters.getNumOfRows();

		log.info("🚀 API 배치 작업 시작 - 시작일: {}, 종료일: {}, 시작페이지: {}, 페이지당 건수: {}",
			startDate, endDate, startPageNo, numOfRows);

		long pageNo = startPageNo;
		boolean hasMoreData = true;
		int totalProcessedItems = 0;

		while (hasMoreData) {
			log.info("📄 현재 처리 중인 페이지: {}", pageNo);

			try {
				String apiResponse = openApiService.callApi(startDate, endDate, pageNo, numOfRows);

				if (isError(apiResponse)) {
					log.error("❌ API 응답 에러 발생, pageNo: {}", pageNo);
					throw new BusinessException(BusinessErrorCode.OPEN_API_ANSWER_ERROR);
				}

				if (isItemsEmpty(apiResponse)) {
					log.info("✅ 모든 데이터 처리 완료. 마지막 페이지: {}", pageNo);
					hasMoreData = false;
					break;
				}

				List<LostItemDto> lostItemDtos = lostItemParseService.parseXmlToDto(apiResponse);

				lostItemBatchService.saveLostItems(lostItemDtos);

				totalProcessedItems += lostItemDtos.size();
				log.info("✅ 페이지 {} 처리 완료: {}건 저장 (누적: {}건)", pageNo, lostItemDtos.size(), totalProcessedItems);

				pageNo++;

			} catch (BusinessException e) {
				log.error("❌ API 호출 또는 데이터 저장 중 오류 발생, pageNo: {}, error: {}",
					pageNo, e.getErrorCode().getMessage());
				throw e;
			} catch (Exception e) {
				log.error("❌ 예상치 못한 오류 발생, pageNo: {}, error: {}", pageNo, e.getMessage());
				throw new BusinessException(BusinessErrorCode.OPEN_API_CALL_ERROR);
			}
		}

		log.info("🎉 전체 배치 작업 완료! 처리된 페이지: {}개, 총 저장된 아이템: {}건",
			pageNo - startPageNo, totalProcessedItems);

		return RepeatStatus.FINISHED;
	}

	private boolean isError(String apiResponse) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new java.io.ByteArrayInputStream(apiResponse.getBytes()));

		String resultCode = document.getElementsByTagName("resultCode").item(0).getTextContent();
		String resultMsg = document.getElementsByTagName("resultMsg").item(0).getTextContent();

		log.info("📡 API 응답 - resultCode: {}, resultMsg: {}", resultCode, resultMsg);
		return !"00".equals(resultCode);
	}

	private boolean isItemsEmpty(String apiResponse) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new java.io.ByteArrayInputStream(apiResponse.getBytes()));

		NodeList items = document.getElementsByTagName("item");
		log.info("📊 현재 페이지 아이템 수: {}", items.getLength());
		return items.getLength() == 0;
	}
}