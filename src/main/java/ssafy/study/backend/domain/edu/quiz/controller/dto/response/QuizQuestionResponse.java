package ssafy.study.backend.domain.edu.quiz.controller.dto.response;

import java.util.List;

import ssafy.study.backend.domain.edu.quiz.entity.QuizQuestion;

public record QuizQuestionResponse(
	Long questionId,
	int questionOrder,
	String question,
	List<QuizOptionResponse> options
) {
	public static QuizQuestionResponse from(QuizQuestion q) {
		return new QuizQuestionResponse(
			q.getId(),
			q.getQuestionOrder(),
			q.getQuestion(),
			q.getOptions().stream()
				.map(QuizOptionResponse::from)
				.toList()
		);
	}
}
