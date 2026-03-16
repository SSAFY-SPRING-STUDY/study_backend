package ssafy.study.backend.domain.edu.quiz.controller.dto.response;

import java.time.LocalDateTime;

import ssafy.study.backend.domain.edu.quiz.entity.QuizAttempt;

public record QuizAttemptSummaryResponse(
	Long attemptId,
	Long memberId,
	String memberName,
	String memberNickname,
	int score,
	boolean passed,
	LocalDateTime attemptedAt
) {
	public static QuizAttemptSummaryResponse from(QuizAttempt attempt) {
		return new QuizAttemptSummaryResponse(
			attempt.getId(),
			attempt.getMember().getId(),
			attempt.getMember().getName(),
			attempt.getMember().getNickname(),
			attempt.getScore(),
			attempt.isPassed(),
			attempt.getCreatedAt()
		);
	}
}
