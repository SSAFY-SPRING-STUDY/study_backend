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
public class QuizOption {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private QuizQuestion quizQuestion;

	@Column(nullable = false, length = 500)
	private String content;

	@Column(nullable = false)
	private boolean isCorrect;

	@Column(nullable = false)
	private int optionOrder;

	@Builder
	private QuizOption(QuizQuestion quizQuestion, String content, boolean isCorrect, int optionOrder) {
		this.quizQuestion = quizQuestion;
		this.content = content;
		this.isCorrect = isCorrect;
		this.optionOrder = optionOrder;
	}
}
