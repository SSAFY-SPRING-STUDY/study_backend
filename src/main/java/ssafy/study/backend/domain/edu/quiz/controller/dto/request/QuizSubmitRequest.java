package ssafy.study.backend.domain.edu.quiz.controller.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record QuizSubmitRequest(
	@NotEmpty
	@Valid
	List<AnswerDto> answers
) {
	public record AnswerDto(
		Long questionId,
		Long selectedOptionId
	) {}
}
