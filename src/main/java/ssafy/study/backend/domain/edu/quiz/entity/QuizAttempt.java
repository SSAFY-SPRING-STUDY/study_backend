package ssafy.study.backend.domain.edu.quiz.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.study.backend.domain.member.entity.Member;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Quiz quiz;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Member member;

	@Column(nullable = false)
	private int score;

	@Column(nullable = false)
	private boolean passed;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL)
	private List<QuizAttemptAnswer> answers = new ArrayList<>();

	@Builder
	private QuizAttempt(Quiz quiz, Member member, int score, boolean passed) {
		this.quiz = quiz;
		this.member = member;
		this.score = score;
		this.passed = passed;
	}

	public void addAnswer(QuizAttemptAnswer answer) {
		this.answers.add(answer);
	}

	public void updateResult(int score, boolean passed) {
		this.score = score;
		this.passed = passed;
	}
}
