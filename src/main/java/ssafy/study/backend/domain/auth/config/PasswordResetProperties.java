package ssafy.study.backend.domain.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.password-reset")
public record PasswordResetProperties(
	long tokenTtl,
	long rateLimitWindow,
	int rateLimitMax
) {
}
