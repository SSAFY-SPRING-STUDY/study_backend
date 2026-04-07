package ssafy.study.backend.global.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubIssueResponse(
	Long number,
	@JsonProperty("html_url") String htmlUrl
) {
}
