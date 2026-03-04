package ssafy.study.backend.domain.edu.post.controller.dto.response;

import ssafy.study.backend.domain.edu.post.entity.Post;

public record PostDetailResponse(
	Long postId,
	String title,
	String content,
	Long authorId,
	String authorName,
	Long curriculumId,
	Integer orderInCurriculum
) {
	public static PostDetailResponse from(Post post) {
		return new PostDetailResponse(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getAuthor().getId(),
			post.getAuthor().getName(),
			post.getCurriculum().getId(),
			post.getCurriculum().getOrderInStudy()
		);
	}
}
