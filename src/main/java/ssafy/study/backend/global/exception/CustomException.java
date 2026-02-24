package ssafy.study.backend.global.exception;

import lombok.Getter;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Getter
public class CustomException extends RuntimeException{
	private final ErrorCode errorCode;

	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
