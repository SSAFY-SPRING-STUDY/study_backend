package ssafy.study.backend.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.jwt")
public record JwtProperties(
	TokenConfig access,
	TokenConfig refresh
) {

	public record TokenConfig(
		String secret,
		long expiration,
		String cookieName
	) {}
}
