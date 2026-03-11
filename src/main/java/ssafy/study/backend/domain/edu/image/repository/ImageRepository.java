package ssafy.study.backend.domain.edu.image.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.edu.image.entity.Image;
import ssafy.study.backend.domain.edu.post.entity.Post;

public interface ImageRepository extends JpaRepository<Image, Long> {
	List<Image> findByPost(Post post);
}
