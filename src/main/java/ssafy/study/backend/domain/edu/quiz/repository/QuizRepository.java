package ssafy.study.backend.domain.edu.quiz.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.study.backend.domain.edu.post.entity.Post;
import ssafy.study.backend.domain.edu.quiz.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

	@EntityGraph(attributePaths = {"questions"})
	Optional<Quiz> findWithQuestionsAndOptionsByPost(Post post);

	Optional<Quiz> findByPost(Post post);

	boolean existsByPost(Post post);

	void deleteByPost(Post post);
}
