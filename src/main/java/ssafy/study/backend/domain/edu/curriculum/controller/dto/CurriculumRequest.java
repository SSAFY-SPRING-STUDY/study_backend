package ssafy.study.backend.domain.study.curriculum.controller.dto;

import ssafy.study.backend.domain.study.curriculum.entity.DifficultyLevel;

public record CurriculumRequest(
	String name,
	String description,
	Integer order,
	DifficultyLevel level
) {
}
