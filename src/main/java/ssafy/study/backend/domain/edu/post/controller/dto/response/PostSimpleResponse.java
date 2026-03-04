package ssafy.study.backend.domain.study.post.controller.dto.response;

import ssafy.study.backend.domain.study.post.entity.Post;

public record PostSimpleResponse(
	Long postId,
	String title,
	Long authorId,
	String authorName,
	Long curriculumId,
	Integer orderInCurriculum
) {
	public static PostSimpleResponse from(Post savedPost) {
		return new PostSimpleResponse(
			savedPost.getId(),
			savedPost.getTitle(),
			savedPost.getAuthor().getId(),
			savedPost.getAuthor().getName(),
			savedPost.getCurriculum().getId(),
			savedPost.getCurriculum().getOrderInStudy()
		);
	}
}
