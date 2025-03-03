package lost_no_more.lost_no_more_batch.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.LocalDate;
import java.util.List;

import lost_no_more.lost_no_more_batch.item.domain.LostItem;
import lost_no_more.lost_no_more_batch.item.repository.CategoryRepository;
import lost_no_more.lost_no_more_batch.item.repository.LostItemRepository;
import lost_no_more.lost_no_more_batch.open_api.job.OpenApiJobConfig;
import lost_no_more.lost_no_more_batch.open_api.service.OpenApiService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBatchTest
@SpringBootTest
@ExtendWith(SpringExtension.class)
class OpenApiJobIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private OpenApiJobConfig openApiJobConfig;

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private OpenApiService openApiService;

    private static final String TARGET_JOB_NAME = "openApiJob";

    @BeforeEach
    void 초기화() {
        String jobName = System.getProperty("job.name");

        assumeTrue(TARGET_JOB_NAME.equals(jobName));

        lostItemRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void 정상적인_API_응답_시_DB_적재_성공() throws Exception {
        String mockApiResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<response>\n" +
                "    <header>\n" +
                "        <resultCode>00</resultCode>\n" +
                "        <resultMsg>OK</resultMsg>\n" +
                "    </header>\n" +
                "    <body>\n" +
                "        <items>\n" +
                "            <item>\n" +
                "                <prdtClNm>가방 > 백팩</prdtClNm>\n" +
                "                <clrNm>검정색</clrNm>\n" +
                "                <fdFilePathImg>http://example.com/image.jpg</fdFilePathImg>\n" +
                "                <fdPrdtNm>나이키 백팩</fdPrdtNm>\n" +
                "                <fdYmd>2024-02-20</fdYmd>\n" +
                "                <depPlace>서울역</depPlace>\n" +
                "            </item>\n" +
                "        </items>\n" +
                "    </body>\n" +
                "</response>";

        when(openApiService.callApi("2024-02-20", "2024-02-21", 1, 10)).thenReturn(mockApiResponse);

        JobExecution jobExecution = jobLauncher.run(openApiJobConfig.openApiJob(), createJobParameters());

        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        jobExecution.getStepExecutions().forEach(stepExecution ->
                assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED")
        );

        List<LostItem> savedItems = lostItemRepository.findAll();
        assertThat(savedItems).hasSize(1);

        LostItem lostItem = savedItems.get(0);
        assertThat(lostItem.getName()).isEqualTo("나이키 백팩");
        assertThat(lostItem.getColor()).isEqualTo("검정색");
        assertThat(lostItem.getImage()).isEqualTo("http://example.com/image.jpg");
        assertThat(lostItem.getDate()).isEqualTo(LocalDate.of(2024, 2, 20));
        assertThat(lostItem.getLocation().getName()).isEqualTo("서울역");
        assertThat(lostItem.getCategory().getName()).isEqualTo("가방");
    }

    @Test
    void API_응답_에러_시_Job_실패() throws Exception {
        String errorResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<response>\n" +
                "    <header>\n" +
                "        <resultCode>99</resultCode>\n" +
                "        <resultMsg>API Error</resultMsg>\n" +
                "    </header>\n" +
                "</response>";

        when(openApiService.callApi("2024-02-20", "2024-02-21", 1, 10)).thenReturn(errorResponse);

        JobExecution jobExecution = jobLauncher.run(openApiJobConfig.openApiJob(), createJobParameters());

        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("FAILED");

        jobExecution.getStepExecutions().forEach(stepExecution ->
                assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("FAILED")
        );

        List<LostItem> savedItems = lostItemRepository.findAll();
        assertThat(savedItems).isEmpty();
    }

    @Test
    void API_응답에_아이템이_없는_경우_Job_성공_및_DB_미적재() throws Exception {
        String emptyResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<response>\n" +
                "    <header>\n" +
                "        <resultCode>00</resultCode>\n" +
                "        <resultMsg>OK</resultMsg>\n" +
                "    </header>\n" +
                "    <body>\n" +
                "        <items>\n" +
                "        </items>\n" +
                "    </body>\n" +
                "</response>";

        when(openApiService.callApi("2024-02-20", "2024-02-21", 1, 10)).thenReturn(emptyResponse);

        JobExecution jobExecution = jobLauncher.run(openApiJobConfig.openApiJob(), createJobParameters());

        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        List<LostItem> savedItems = lostItemRepository.findAll();
        assertThat(savedItems).isEmpty();
    }

    // ✅ 공통 JobParameters 생성 메서드
    private JobParameters createJobParameters() {
        return new JobParametersBuilder()
                .addString("START_DATE", "2024-02-20")
                .addString("END_DATE", "2024-02-21")
                .addLong("PAGE_NO", 1L)
                .addLong("NUM_OF_ROWS", 10L)
                .toJobParameters();
    }
}