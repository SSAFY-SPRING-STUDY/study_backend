package ssafy.study.backend.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 프론트엔드 base URL.
 * <p>
 * 메일 본문·OAuth redirect 등 백엔드가 프론트로 사용자를 보내야 할 때 사용한다.
 * 경로는 도메인 코드(예: 비밀번호 재설정 페이지 경로)에서 상수로 조합한다.
 */
@ConfigurationProperties(prefix = "custom.front")
public record FrontProperties(
	String baseUrl
) {
}
