package ssafy.study.backend.domain.edu.curriculum.controller.dto;

import ssafy.study.backend.domain.edu.curriculum.entity.Curriculum;

public record CurriculumResponse (
	Long id,
	String title,
	String description,
	Integer order,
	Integer postsCount
){
	public static CurriculumResponse from(Curriculum curriculum) {
		return new CurriculumResponse(
			curriculum.getId(),
			curriculum.getName(),
			curriculum.getDescription(),
			curriculum.getOrderInStudy(),
			curriculum.getPostsCount()
		);
	}
}
