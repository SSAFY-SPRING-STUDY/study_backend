package ssafy.study.backend.domain.edu.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import ssafy.study.backend.domain.edu.curriculum.entity.Curriculum;
import ssafy.study.backend.domain.edu.curriculum.repository.CurriculumRepository;
import ssafy.study.backend.domain.edu.image.service.ImageService;
import ssafy.study.backend.domain.edu.post.controller.dto.request.PostRequest;
import ssafy.study.backend.domain.edu.post.controller.dto.response.PostDetailResponse;
import ssafy.study.backend.domain.edu.post.controller.dto.response.PostSimpleResponse;
import ssafy.study.backend.domain.edu.post.entity.Post;
import ssafy.study.backend.domain.edu.post.repository.PostRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.fixture.CurriculumFixture;
import ssafy.study.backend.fixture.MemberFixture;
import ssafy.study.backend.fixture.PostFixture;
import ssafy.study.backend.fixture.StudyFixture;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

	@InjectMocks
	private PostService postService;

	@Mock
	private EntityManager entityManager;

	@Mock
	private PostRepository postRepository;

	@Mock
	private ImageService imageService;

	@Mock
	private CurriculumRepository curriculumRepository;

	@Test
	@DisplayName("커리큘럼별 게시글 목록 조회 성공")
	void 커리큘럼별_게시글_목록_조회_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Curriculum curriculum = CurriculumFixture.curriculum(1L, StudyFixture.study(1L));
		Post post1 = PostFixture.post(1L, author, curriculum);
		Post post2 = PostFixture.post(2L, author, curriculum);
		given(curriculumRepository.existsById(1L)).willReturn(true);
		given(postRepository.findByCurriculumIdOrderByOrderInCurriculumAsc(1L)).willReturn(List.of(post1, post2));

		// when
		List<PostSimpleResponse> result = postService.getPostsByCurriculum(1L);

		// then
		assertThat(result).hasSize(2);
	}

	@Test
	@DisplayName("커리큘럼별 게시글 목록 조회 실패 - 존재하지 않는 커리큘럼")
	void 커리큘럼별_게시글_목록_조회_실패_존재하지_않는_커리큘럼() {
		// given
		given(curriculumRepository.existsById(999L)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> postService.getPostsByCurriculum(999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CURRICULUM_NOT_FOUND);
	}

	@Test
	@DisplayName("게시글 생성 성공")
	void 게시글_생성_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Curriculum curriculum = CurriculumFixture.curriculum(1L, StudyFixture.study(1L));
		PostRequest request = new PostRequest("스프링 입문 1강", "스프링 기초 내용입니다.", null);
		Post savedPost = PostFixture.post(1L, author, curriculum);

		given(entityManager.getReference(Member.class, 1L)).willReturn(author);
		given(curriculumRepository.findById(1L)).willReturn(Optional.of(curriculum));
		given(postRepository.save(any(Post.class))).willReturn(savedPost);

		// when
		PostSimpleResponse result = postService.createPost(1L, 1L, request);

		// then
		assertThat(result.postId()).isEqualTo(1L);
		assertThat(result.title()).isEqualTo(savedPost.getTitle());
		assertThat(result.authorId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("게시글 생성 실패 - 존재하지 않는 커리큘럼")
	void 게시글_생성_실패_존재하지_않는_커리큘럼() {
		// given
		Member author = MemberFixture.member(1L);
		PostRequest request = new PostRequest("스프링 입문 1강", "내용", null);
		given(entityManager.getReference(Member.class, 1L)).willReturn(author);
		given(curriculumRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> postService.createPost(1L, 999L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CURRICULUM_NOT_FOUND);
	}

	@Test
	@DisplayName("게시글 단건 조회 성공")
	void 게시글_단건_조회_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Curriculum curriculum = CurriculumFixture.curriculum(1L, StudyFixture.study(1L));
		Post post = PostFixture.post(1L, author, curriculum);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));

		// when
		PostDetailResponse result = postService.getPost(1L);

		// then
		assertThat(result.postId()).isEqualTo(1L);
		assertThat(result.title()).isEqualTo(post.getTitle());
	}

	@Test
	@DisplayName("게시글 단건 조회 실패 - 존재하지 않는 게시글")
	void 게시글_단건_조회_실패_존재하지_않는_게시글() {
		// given
		given(postRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> postService.getPost(999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
	}

	@Test
	@DisplayName("게시글 수정 성공")
	void 게시글_수정_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Curriculum curriculum = CurriculumFixture.curriculum(1L, StudyFixture.study(1L));
		Post post = PostFixture.post(1L, author, curriculum);
		PostRequest request = new PostRequest("수정된 제목", "수정된 내용", null);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));

		// when
		PostSimpleResponse result = postService.updatePost(1L, 1L, request);

		// then
		assertThat(result.title()).isEqualTo("수정된 제목");
	}

	@Test
	@DisplayName("게시글 수정 실패 - 존재하지 않는 게시글")
	void 게시글_수정_실패_존재하지_않는_게시글() {
		// given
		PostRequest request = new PostRequest("수정된 제목", "수정된 내용", null);
		given(postRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> postService.updatePost(1L, 999L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
	}

	@Test
	@DisplayName("게시글 수정 실패 - 작성자가 아닌 경우")
	void 게시글_수정_실패_작성자가_아닌_경우() {
		// given
		Member author = MemberFixture.member(1L);
		Curriculum curriculum = CurriculumFixture.curriculum(1L, StudyFixture.study(1L));
		Post post = PostFixture.post(1L, author, curriculum);
		PostRequest request = new PostRequest("수정된 제목", "수정된 내용", null);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));

		// when & then (authorId가 다른 2L로 요청)
		assertThatThrownBy(() -> postService.updatePost(2L, 1L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_ACCESS_DENIED);
	}

	@Test
	@DisplayName("게시글 삭제 성공")
	void 게시글_삭제_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Curriculum curriculum = CurriculumFixture.curriculum(1L, StudyFixture.study(1L));
		Post post = PostFixture.post(1L, author, curriculum);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		willDoNothing().given(postRepository).delete(post);

		// when & then
		assertThatNoException().isThrownBy(() -> postService.deletePost(1L, 1L));
		then(postRepository).should().delete(post);
	}

	@Test
	@DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
	void 게시글_삭제_실패_존재하지_않는_게시글() {
		// given
		given(postRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> postService.deletePost(1L, 999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
	}

	@Test
	@DisplayName("게시글 삭제 실패 - 작성자가 아닌 경우")
	void 게시글_삭제_실패_작성자가_아닌_경우() {
		// given
		Member author = MemberFixture.member(1L);
		Curriculum curriculum = CurriculumFixture.curriculum(1L, StudyFixture.study(1L));
		Post post = PostFixture.post(1L, author, curriculum);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));

		// when & then (authorId가 다른 2L로 요청)
		assertThatThrownBy(() -> postService.deletePost(2L, 1L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_ACCESS_DENIED);
	}
}
