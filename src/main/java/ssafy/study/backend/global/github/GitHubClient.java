package ssafy.study.backend.global.github;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;
import ssafy.study.backend.global.github.dto.GitHubIssueRequest;
import ssafy.study.backend.global.github.dto.GitHubIssueResponse;

@Slf4j
@Component
public class GitHubClient {

	private static final String BASE_URL = "https://api.github.com";

	private final RestClient restClient;

	public GitHubClient(GitHubProperties props) {
		this.restClient = RestClient.builder()
			.baseUrl(BASE_URL)
			.defaultHeader("Authorization", "Bearer " + props.token())
			.defaultHeader("Accept", "application/vnd.github+json")
			.build();
	}

	/** GitHub 조직 레포에 Issue 생성 */
	public GitHubIssueResponse createIssue(String org, String repo, GitHubIssueRequest request) {
		try {
			return restClient.post()
				.uri("/repos/{org}/{repo}/issues", org, repo)
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(GitHubIssueResponse.class);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.GITHUB_ISSUE_CREATION_FAILED);
		}
	}

	/** GitHub Issue body 업데이트 (best-effort, 실패 시 경고 로그만 남김) */
	public void updateIssueBody(String org, String repo, Long issueNumber, String newBody) {
		try {
			restClient.patch()
				.uri("/repos/{org}/{repo}/issues/{number}", org, repo, issueNumber)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("body", newBody))
				.retrieve()
				.toBodilessEntity();
		} catch (Exception e) {
			log.warn("GitHub Issue body 업데이트 실패. org={}, repo={}, issue={}", org, repo, issueNumber, e);
		}
	}
}
