package ssafy.study.backend.global.exception.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	// General Server Error
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 입력 값입니다."),
	INVALID_ENUM_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 enum값입니다." ),

	// Authentication Errors
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

	// Database Errors
	DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다."),

	// Member Errors
	EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
	USERNAME_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 사용자 이름입니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),

	// Authentication Errors
	BAD_CREDENTIAL(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,"토큰이 만료되었습니다. 다시 로그인해주세요."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다. 다시 로그인해주세요."),
	REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "RefreshToken이 존재하지 않습니다."),
	INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "기존 비밀번호의 정보가 올바르지 않습니다." ),
	SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "새로운 비밀번호는 이전 비밀번호와 달라야 합니다."),

	// CURRICULUM Errors
	CURRICULUM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 커리큘럼입니다." ),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시물입니다." ),
	POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "게시물 작성자가 아닙니다." ),

	CURRICULUM_ORDER_CONSTRAINT_VIOLATION(HttpStatus.CONFLICT, "커리큘럼의 orderInStudy는 같은 스터디 내에서 중복될 수 없습니다." ),

	// STUDY Errors
	STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 스터디입니다." ),

	// IMAGE Errors
	IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 이미지입니다." ),

	// NOTICE Errors
	NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공지사항입니다." ),
	NOTICE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "공지사항 작성자가 아닙니다." ),

	// NOTIFICATION Errors
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),

	// COMMENT Errors
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
	COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근할 수 있는 권한이 없습니다."),
	RECOMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 대댓글입니다."),

	// QUIZ Errors
	QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "퀴즈가 존재하지 않습니다."),
	QUIZ_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 생성된 퀴즈가 있습니다."),
	QUIZ_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "퀴즈 생성에 실패했습니다. 잠시 후 다시 시도해주세요."),
	QUIZ_INVALID_SUBMISSION(HttpStatus.BAD_REQUEST, "제출한 답안이 유효하지 않습니다."),
	QUIZ_ATTEMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "퀴즈 시도 이력이 없습니다.");



	private final HttpStatus httpStatus;
	private final String message;
}
