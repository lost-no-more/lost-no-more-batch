package lost_no_more.lost_no_more_batch.open_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lost_no_more.lost_no_more_batch.global.exception.BusinessException;
import lost_no_more.lost_no_more_batch.global.exception.code.BusinessErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiService {

    @Value("${open-api.url}")
    private String url;

    @Value("${open-api.secret-key}")
    private String key;

    public String callApi(String startDate, String endDate, long pageNo, long numOfRows) throws Exception {
        String apiUrl = buildApiUrl(startDate, endDate, pageNo, numOfRows);

        log.info("🌐 API 호출 시작 - URL: {}", maskUrl(apiUrl));

        HttpURLConnection conn = null;
        BufferedReader rd = null;

        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            log.info("📡 API 응답 코드: {}", responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("❌ API 호출 실패: HTTP {} - {}", responseCode, conn.getResponseMessage());
                throw new BusinessException(BusinessErrorCode.OPEN_API_CALL_ERROR);
            }

            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            String response = sb.toString();
            log.info("✅ API 응답 수신 완료 - 데이터 크기: {} bytes", response.length());

            return response;

        } catch (Exception e) {
            log.error("❌ API 호출 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(BusinessErrorCode.OPEN_API_CALL_ERROR);
        } finally {
            if (rd != null) {
                try {
                    rd.close();
                } catch (Exception e) {
                    log.warn("BufferedReader 닫기 실패: {}", e.getMessage());
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String buildApiUrl(String startDate, String endDate, long pageNo, long numOfRows) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(url);

        urlBuilder.append("?").append(URLEncoder.encode("serviceKey", StandardCharsets.UTF_8)).append("=").append(key);
        urlBuilder.append("&").append(URLEncoder.encode("START_YMD", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(startDate, StandardCharsets.UTF_8));
        urlBuilder.append("&").append(URLEncoder.encode("END_YMD", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(endDate, StandardCharsets.UTF_8));
        urlBuilder.append("&").append(URLEncoder.encode("pageNo", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8));
        urlBuilder.append("&").append(URLEncoder.encode("numOfRows", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(String.valueOf(numOfRows), StandardCharsets.UTF_8));

        return urlBuilder.toString();
    }

    private String maskUrl(String url) {
        if (url.contains("serviceKey=")) {
            return url.replaceAll("serviceKey=[^&]*", "serviceKey=***MASKED***");
        }
        return url;
    }
}