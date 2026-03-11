package ssafy.study.backend.domain.edu.post.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ssafy.study.backend.domain.edu.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
	@Query("SELECT COALESCE(MAX(p.orderInCurriculum), 0) FROM Post p WHERE p.curriculum.id = :curriculumId")
	int findMaxOrderInCurriculum(@Param("curriculumId") Long curriculumId);

	List<Post> findByCurriculumIdOrderByOrderInCurriculumAsc(Long curriculumId);
}
