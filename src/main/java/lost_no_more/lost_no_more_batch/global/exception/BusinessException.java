package lost_no_more.lost_no_more_batch.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lost_no_more.lost_no_more_batch.global.exception.code.DefaultErrorCode;

@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {

    private final DefaultErrorCode errorCode;
}
