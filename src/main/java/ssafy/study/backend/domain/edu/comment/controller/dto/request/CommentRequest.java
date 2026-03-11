package ssafy.study.backend.domain.edu.comment.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
	@NotBlank(message = "댓글 내용을 입력해주세요.")
	String content
) {
}