package ssafy.study.backend.global.exception.handler;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;
import ssafy.study.backend.global.response.ApiResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	/**
	 * CustomException 처리
	 */
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ApiResponse.error(errorCode));
	}

	/**
	 * DataIntegrityViolationException 처리(DB 제약 조건 위반)
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
		ErrorCode errorCode = ErrorCode.DATABASE_ERROR;
		log.error("DB 제약 조건 위반", e); // DB 스키마 변경 이력 또는 DTO 검증 확인
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ApiResponse.error(errorCode));
	}

	/**
	 * MethodArgumentNotValidException 처리(DTO 검증 실패)
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<List<String>>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e) {
		ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
		// 필드 오류 메시지 정렬 및 생성(
		List<String> errors = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.sorted(
				Comparator.comparing(FieldError::getField)
					.thenComparing(error -> Optional.ofNullable(error.getDefaultMessage()).orElse(""))
			)
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.toList();
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ApiResponse.error(errorCode, errors));
	}

	/**
	 * 모든 예외 처리 (예상치 못한 서버 오류)
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		log.error("서버 내부 에러", e);
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ApiResponse.error(errorCode));
	}
}
