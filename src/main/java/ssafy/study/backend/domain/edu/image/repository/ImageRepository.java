package ssafy.study.backend.domain.edu.image.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ssafy.study.backend.domain.edu.image.entity.Image;
import ssafy.study.backend.domain.edu.post.entity.Post;

public interface ImageRepository extends JpaRepository<Image, Long> {

	List<Image> findByPost(Post post);

	List<Image> findByPostIsNullAndCreatedAtBefore(LocalDateTime cutoff);

	@Query("SELECT i FROM Image i WHERE i.id IN :ids AND i.post IS NULL")
	List<Image> findByIdsAndPostIsNull(@Param("ids") List<Long> ids);
}
