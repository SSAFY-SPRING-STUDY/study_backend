package ssafy.study.backend.domain.study.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.study.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
