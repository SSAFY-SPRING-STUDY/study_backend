package ssafy.study.backend.domain.edu.quiz.controller.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import ssafy.study.backend.domain.edu.quiz.entity.Quiz;

public record QuizResponse(
	Long quizId,
	Long postId,
	LocalDateTime createdAt,
	List<QuizQuestionResponse> questions
) {
	public static QuizResponse from(Quiz quiz) {
		return new QuizResponse(
			quiz.getId(),
			quiz.getPost().getId(),
			quiz.getCreatedAt(),
			quiz.getQuestions().stream()
				.map(QuizQuestionResponse::from)
				.toList()
		);
	}
}
