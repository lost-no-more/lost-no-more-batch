package lost_no_more.lost_no_more_batch.global.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessErrorCode implements DefaultErrorCode {

    OPEN_API_ANSWER_ERROR("API 응답 오류 발생"),
    OPEN_API_CALL_ERROR("API 호출 실패"),
    ;

    private String message;
}
