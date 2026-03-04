package ssafy.study.backend.domain.edu.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.edu.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
