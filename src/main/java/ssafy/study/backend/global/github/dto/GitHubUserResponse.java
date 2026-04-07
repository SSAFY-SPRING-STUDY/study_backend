package ssafy.study.backend.global.github.dto;

public record GitHubUserResponse(
	String id,
	String login,
	String email,
	String name
) {
}
