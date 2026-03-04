package ssafy.study.backend.domain.edu.study.controller.dto.request;

import ssafy.study.backend.domain.edu.study.entity.Study;

public record StudyResponse (
	Long id,
	String name,
	String description,
	String level,
	String type
) {
	public static StudyResponse from(Study savedStudy) {
		return new StudyResponse(
			savedStudy.getId(),
			savedStudy.getName(),
			savedStudy.getDescription(),
			savedStudy.getLevel().name(),
			savedStudy.getType().name()
		);
	}
}
