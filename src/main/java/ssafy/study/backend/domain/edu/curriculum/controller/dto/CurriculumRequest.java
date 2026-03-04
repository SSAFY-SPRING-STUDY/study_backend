package ssafy.study.backend.domain.edu.curriculum.controller.dto;

public record CurriculumRequest(
	String name,
	String description,
	Integer order
) {
}
