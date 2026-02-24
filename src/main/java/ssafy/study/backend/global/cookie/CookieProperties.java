package ssafy.study.backend.global.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.cookie")
public record CookieProperties(
	String domain,
	boolean secure,
	String sameSite
) {
}