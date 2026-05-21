package ssafy.study.backend.global.github;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github")
public record GitHubProperties(
	String clientId,
	String clientSecret,
	String redirectUri,
	String connectRedirectUri
) {
}