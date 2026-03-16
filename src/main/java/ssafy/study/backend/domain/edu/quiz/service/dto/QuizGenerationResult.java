package ssafy.study.backend.domain.edu.quiz.service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QuizGenerationResult(List<QuestionDto> questions) {

	public record QuestionDto(
		String question,
		List<OptionDto> options
	) {}

	public record OptionDto(
		String content,
		@JsonProperty("isCorrect") boolean isCorrect
	) {}
}
