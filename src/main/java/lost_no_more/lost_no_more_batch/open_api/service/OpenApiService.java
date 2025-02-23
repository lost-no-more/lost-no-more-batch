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

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiService {

    @Value("${open-api.url}")
    private String url;

    @Value("${open-api.secret-key}")
    private String key;

    public String callApi(String startDate, String endDate, int pageNo, int numOfRows) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + key);
        urlBuilder.append("&" + URLEncoder.encode("START_YMD", "UTF-8") + "=" + URLEncoder.encode(startDate, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("END_YMD", "UTF-8") + "=" + URLEncoder.encode(endDate, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(pageNo), "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(numOfRows), "UTF-8"));

        URL apiUrl = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        log.info("API 응답 코드: {}", responseCode);

        if (responseCode != 200) {
            log.error("API 호출 실패: 응답 코드 = {}", responseCode);
            throw new BusinessException(BusinessErrorCode.OPEN_API_CALL_ERROR);
        }

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        return sb.toString();
    }
}