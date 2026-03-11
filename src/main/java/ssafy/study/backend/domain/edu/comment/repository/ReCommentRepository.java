package ssafy.study.backend.domain.edu.comment.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.edu.comment.entity.ReComment;

public interface ReCommentRepository extends JpaRepository<ReComment, Long> {

	@EntityGraph(attributePaths = {"author"})
	Optional<ReComment> findReCommentById(Long id);

	@EntityGraph(attributePaths = {"author"})
	Page<ReComment> findByCommentId(Long commentId, Pageable pageable);
}
