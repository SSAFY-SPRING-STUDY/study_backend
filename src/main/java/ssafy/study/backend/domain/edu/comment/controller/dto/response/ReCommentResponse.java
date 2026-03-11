package ssafy.study.backend.domain.edu.comment.controller.dto.response;

import java.time.LocalDateTime;

import ssafy.study.backend.domain.edu.comment.entity.ReComment;

public record ReCommentResponse(
	Long reCommentId,
	String content,
	Long authorId,
	String authorName,
	Long parentCommentId,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static ReCommentResponse from(ReComment reComment) {
		return new ReCommentResponse(
			reComment.getId(),
			reComment.getContent(),
			reComment.getAuthor().getId(),
			reComment.getAuthor().getName(),
			reComment.getComment().getId(),
			reComment.getCreatedAt(),
			reComment.getUpdatedAt()
		);
	}
}
