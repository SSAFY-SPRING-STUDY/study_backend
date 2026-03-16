package ssafy.study.backend.domain.edu.quiz.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Quiz quiz;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String question;

	@Column(nullable = false)
	private int questionOrder;

	@BatchSize(size = 10)
	@OneToMany(mappedBy = "quizQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("optionOrder ASC")
	private List<QuizOption> options = new ArrayList<>();

	@Builder
	private QuizQuestion(Quiz quiz, String question, int questionOrder) {
		this.quiz = quiz;
		this.question = question;
		this.questionOrder = questionOrder;
	}

	public void addOption(QuizOption option) {
		this.options.add(option);
	}
}
