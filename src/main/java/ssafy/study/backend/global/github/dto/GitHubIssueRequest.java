package ssafy.study.backend.global.github.dto;

import java.util.List;

public record GitHubIssueRequest(
	String title,
	String body,
	List<String> assignees
) {
}
