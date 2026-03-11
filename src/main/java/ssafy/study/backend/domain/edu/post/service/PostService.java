package ssafy.study.backend.domain.edu.post.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.curriculum.entity.Curriculum;
import ssafy.study.backend.domain.edu.curriculum.repository.CurriculumRepository;
import ssafy.study.backend.domain.edu.image.repository.ImageRepository;
import ssafy.study.backend.domain.edu.post.controller.dto.request.PostRequest;
import ssafy.study.backend.domain.edu.post.controller.dto.response.PostDetailResponse;
import ssafy.study.backend.domain.edu.post.controller.dto.response.PostSimpleResponse;
import ssafy.study.backend.domain.edu.post.entity.Post;
import ssafy.study.backend.domain.edu.post.repository.PostRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
	private final EntityManager entityManager;
	private final PostRepository postRepository;
	private final ImageRepository imageRepository;
	private final CurriculumRepository curriculumRepository;

	@Transactional
	public PostSimpleResponse createPost(Long authorId, Long curriculumId, PostRequest request) {
		Member author = entityManager.getReference(Member.class, authorId);

		Curriculum curriculum = curriculumRepository.findById(curriculumId)
			.orElseThrow(() -> new CustomException(ErrorCode.CURRICULUM_NOT_FOUND));

		int nextOrder = curriculum.incrementPostsCount();//postRepository.findMaxOrderInCurriculum(curriculumId) + 1;

		Post newPost = Post.builder()
			.title(request.title())
			.content(request.content())
			.author(author)
			.curriculum(curriculum)
			.orderInCurriculum(nextOrder)
			.build();

		Post savedPost = postRepository.save(newPost);
		return PostSimpleResponse.from(savedPost);
	}

	public PostDetailResponse getPost(Long postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		return PostDetailResponse.from(post);
	}

	@Transactional
	public PostSimpleResponse updatePost(Long authorId, Long postId, PostRequest request) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		if(!post.isAuthor(authorId)) {
			throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
		}

		post.update(request.title(), request.content());
		return PostSimpleResponse.from(post);
	}

	public List<PostSimpleResponse> getPostsByCurriculum(Long curriculumId) {
		if (!curriculumRepository.existsById(curriculumId)) {
			throw new CustomException(ErrorCode.CURRICULUM_NOT_FOUND);
		}
		return postRepository.findByCurriculumIdOrderByOrderInCurriculumAsc(curriculumId)
			.stream()
			.map(PostSimpleResponse::from)
			.toList();
	}

	@Transactional
	public void deletePost(Long authorId, Long postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		if(!post.isAuthor(authorId)) {
			throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
		}

		// todo: 게시글 삭제 시 연관된 이미지도 함께 삭제하는 로직 추가 필요

		postRepository.delete(post);
	}
}
