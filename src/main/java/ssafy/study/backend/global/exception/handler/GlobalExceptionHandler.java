package ssafy.study.backend.global.exception.handler;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import tools.jackson.databind.exc.InvalidFormatException;
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
		Throwable root = NestedExceptionUtils.getRootCause(e);
		String message = root != null ? root.getMessage() : e.getMessage();

		log.error("DB 제약 조건 위반", e);
		if(message.contains("uc_curriculum_orderinstudy")) {
			log.error("커리큘럼 orderInStudy 제약 조건 위반 감지: ", e);
			errorCode = ErrorCode.CURRICULUM_ORDER_CONSTRAINT_VIOLATION;
		}

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
	 * HttpMessageNotReadableException 처리(잘못된 JSON 형식 또는 타입 불일치)
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidFormat(
		HttpMessageNotReadableException e) {

		Throwable cause = e.getCause();

		ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

		if (cause instanceof InvalidFormatException ex) {
			if (ex.getTargetType().isEnum()) {
				errorCode = ErrorCode.INVALID_ENUM_VALUE;
			}
		}

		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.error(errorCode));
	}

	@ExceptionHandler(AuthorizationDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthorizationDeniedException(
		AuthorizationDeniedException e) {

		ErrorCode errorCode = ErrorCode.FORBIDDEN;

		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ApiResponse.error(errorCode));
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
