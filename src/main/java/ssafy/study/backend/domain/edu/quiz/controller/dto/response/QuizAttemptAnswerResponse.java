package ssafy.study.backend.domain.edu.quiz.controller.dto.response;

import ssafy.study.backend.domain.edu.quiz.entity.QuizAttemptAnswer;
import ssafy.study.backend.domain.edu.quiz.entity.QuizOption;

public record QuizAttemptAnswerResponse(
	Long questionId,
	String question,
	Long selectedOptionId,
	Long correctOptionId,
	boolean correct
) {
	public static QuizAttemptAnswerResponse from(QuizAttemptAnswer answer) {
		Long correctOptionId = answer.getQuizQuestion().getOptions().stream()
			.filter(QuizOption::isCorrect)
			.map(QuizOption::getId)
			.findFirst()
			.orElse(null);

		return new QuizAttemptAnswerResponse(
			answer.getQuizQuestion().getId(),
			answer.getQuizQuestion().getQuestion(),
			answer.getQuizOption().getId(),
			correctOptionId,
			answer.isCorrect()
		);
	}
}
