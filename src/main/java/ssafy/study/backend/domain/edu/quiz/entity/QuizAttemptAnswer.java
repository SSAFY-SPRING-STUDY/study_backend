package ssafy.study.backend.domain.edu.quiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAttemptAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private QuizAttempt quizAttempt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private QuizQuestion quizQuestion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private QuizOption quizOption;

	@Column(nullable = false)
	private boolean isCorrect;

	@Builder
	private QuizAttemptAnswer(QuizAttempt quizAttempt, QuizQuestion quizQuestion, QuizOption quizOption,
		boolean isCorrect) {
		this.quizAttempt = quizAttempt;
		this.quizQuestion = quizQuestion;
		this.quizOption = quizOption;
		this.isCorrect = isCorrect;
	}
}
