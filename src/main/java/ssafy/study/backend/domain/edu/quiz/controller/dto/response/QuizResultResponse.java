package ssafy.study.backend.domain.edu.quiz.controller.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import ssafy.study.backend.domain.edu.quiz.entity.QuizAttempt;

public record QuizResultResponse(
	Long attemptId,
	int score,
	int totalQuestions,
	boolean passed,
	LocalDateTime createdAt,
	List<QuizAttemptAnswerResponse> results
) {
	public static QuizResultResponse from(QuizAttempt attempt) {
		return new QuizResultResponse(
			attempt.getId(),
			attempt.getScore(),
			attempt.getAnswers().size(),
			attempt.isPassed(),
			attempt.getCreatedAt(),
			attempt.getAnswers().stream()
				.map(QuizAttemptAnswerResponse::from)
				.toList()
		);
	}
}
