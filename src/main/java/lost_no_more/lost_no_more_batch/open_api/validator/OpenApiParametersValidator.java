package lost_no_more.lost_no_more_batch.open_api.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;

@Component
public class OpenApiParametersValidator implements JobParametersValidator {
    
    @Override
    public void validate(@Nullable JobParameters parameters) throws JobParametersInvalidException {
        
        if (parameters == null) {
            throw new JobParametersInvalidException("Job 파라미터가 없습니다");
        }
        
        validateDateParameter(parameters, "START_DATE");
        validateDateParameter(parameters, "END_DATE");
        
        validateIntegerParameter(parameters, "PAGE_NO", 1, Integer.MAX_VALUE);
        validateIntegerParameter(parameters, "NUM_OF_ROWS", 1, 10000);
        
        validateDateRange(parameters);
    }
    
    private void validateDateParameter(JobParameters parameters, String paramName) 
            throws JobParametersInvalidException {
        
        String dateStr = parameters.getString(paramName);
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new JobParametersInvalidException(paramName + " 파라미터는 필수값입니다");
        }
        
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (DateTimeParseException e) {
            throw new JobParametersInvalidException(
                paramName + " 날짜 형식이 올바르지 않습니다. yyyyMMdd 형식으로 입력하세요: " + dateStr);
        }
    }
    
    private void validateIntegerParameter(JobParameters parameters, String paramName, 
                                        int min, int max) throws JobParametersInvalidException {
        
        Long value = parameters.getLong(paramName);
        if (value == null) {
            throw new JobParametersInvalidException(paramName + " 파라미터는 필수값입니다");
        }
        
        if (value < min || value > max) {
            throw new JobParametersInvalidException(
                paramName + " 값이 허용 범위를 벗어났습니다: " + value + 
                " (허용 범위: " + min + " ~ " + max + ")");
        }
    }
    
    private void validateDateRange(JobParameters parameters) throws JobParametersInvalidException {
        
        String startDateStr = parameters.getString("START_DATE");
        String endDateStr = parameters.getString("END_DATE");
        
        LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        if (startDate.isAfter(endDate)) {
            throw new JobParametersInvalidException(
                "시작일이 종료일보다 늦을 수 없습니다. 시작일: " + startDateStr + ", 종료일: " + endDateStr);
        }
    }
}