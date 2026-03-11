package ssafy.study.backend.domain.edu.comment.controller.dto.response;

import java.time.LocalDateTime;

import ssafy.study.backend.domain.edu.comment.entity.Comment;

public record CommentResponse(
	Long commentId,
	String content,
	Long authorId,
	String authorName,
	Long postId,
	Long reCommentCount,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static CommentResponse from(Comment comment) {
		return new CommentResponse(
			comment.getId(),
			comment.getContent(),
			comment.getAuthor().getId(),
			comment.getAuthor().getName(),
			comment.getPost().getId(),
			comment.getReCommentCount(),
			comment.getCreatedAt(),
			comment.getUpdatedAt()
		);
	}
}