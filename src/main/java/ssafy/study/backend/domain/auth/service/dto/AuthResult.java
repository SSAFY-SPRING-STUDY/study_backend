package ssafy.study.backend.domain.auth.service.dto;

public record AuthResult(
	String accessToken,
	String refreshToken
) {
}
