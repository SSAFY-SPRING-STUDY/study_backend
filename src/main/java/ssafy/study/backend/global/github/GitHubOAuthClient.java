package ssafy.study.backend.global.github;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import ssafy.study.backend.global.github.dto.GitHubTokenResponse;
import ssafy.study.backend.global.github.dto.GitHubUserResponse;

@Component
public class GitHubOAuthClient {

	private static final String OAUTH_BASE = "https://github.com";
	private static final String API_BASE = "https://api.github.com";

	private final RestClient oauthClient;
	private final RestClient apiClient;
	private final GitHubProperties props;

	public GitHubOAuthClient(GitHubProperties props) {
		this.props = props;
		this.oauthClient = RestClient.builder()
			.baseUrl(OAUTH_BASE)
			.defaultHeader("Accept", "application/json")
			.build();
		this.apiClient = RestClient.builder()
			.baseUrl(API_BASE)
			.defaultHeader("Accept", "application/vnd.github+json")
			.build();
	}

	/** 로그인용 OAuth App 자격증명으로 code → access_token 교환 */
	public GitHubTokenResponse exchangeLoginCode(String code) {
		return oauthClient.post()
			.uri("/login/oauth/access_token")
			.body(Map.of(
				"client_id", props.clientId(),
				"client_secret", props.clientSecret(),
				"code", code,
				"redirect_uri", props.redirectUri()
			))
			.retrieve()
			.body(GitHubTokenResponse.class);
	}

	/** 연결용 OAuth App 자격증명으로 code → access_token 교환 */
	public GitHubTokenResponse exchangeConnectCode(String code) {
		return oauthClient.post()
			.uri("/login/oauth/access_token")
			.body(Map.of(
				"client_id", props.connectClientId(),
				"client_secret", props.connectClientSecret(),
				"code", code,
				"redirect_uri", props.connectRedirectUri()
			))
			.retrieve()
			.body(GitHubTokenResponse.class);
	}

	/** GitHub access_token으로 /user API 호출 → 사용자 정보 반환 */
	public GitHubUserResponse getUser(String accessToken) {
		return apiClient.get()
			.uri("/user")
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.body(GitHubUserResponse.class);
	}
}
