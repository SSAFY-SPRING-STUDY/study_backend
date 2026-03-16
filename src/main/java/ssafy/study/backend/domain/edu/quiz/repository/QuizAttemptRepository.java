package ssafy.study.backend.domain.edu.quiz.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssafy.study.backend.domain.edu.quiz.entity.Quiz;
import ssafy.study.backend.domain.edu.quiz.entity.QuizAttempt;
import ssafy.study.backend.domain.member.entity.Member;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

	@EntityGraph(attributePaths = {"answers", "answers.quizQuestion", "answers.quizOption"})
	Optional<QuizAttempt> findTopByQuizAndMemberOrderByCreatedAtDesc(Quiz quiz, Member member);

	@Query(value = """
		SELECT qa FROM QuizAttempt qa
		JOIN FETCH qa.member
		WHERE qa.quiz = :quiz
		AND qa.id IN (
		    SELECT MAX(qa2.id) FROM QuizAttempt qa2
		    WHERE qa2.quiz = :quiz
		    GROUP BY qa2.member
		)
		ORDER BY qa.createdAt DESC
		""",
		countQuery = """
		SELECT COUNT(qa) FROM QuizAttempt qa
		WHERE qa.quiz = :quiz
		AND qa.id IN (
		    SELECT MAX(qa2.id) FROM QuizAttempt qa2
		    WHERE qa2.quiz = :quiz
		    GROUP BY qa2.member
		)
		""")
	Page<QuizAttempt> findLatestAttemptPerMemberByQuiz(@Param("quiz") Quiz quiz, Pageable pageable);
}
