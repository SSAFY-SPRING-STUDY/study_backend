package ssafy.study.backend.domain.edu.comment.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.EntityManager;
import ssafy.study.backend.domain.edu.comment.controller.dto.request.CommentRequest;
import ssafy.study.backend.domain.edu.comment.controller.dto.response.CommentResponse;
import ssafy.study.backend.domain.edu.comment.controller.dto.response.ReCommentResponse;
import ssafy.study.backend.domain.edu.comment.entity.Comment;
import ssafy.study.backend.domain.edu.comment.entity.ReComment;
import ssafy.study.backend.domain.edu.comment.repository.CommentRepository;
import ssafy.study.backend.domain.edu.comment.repository.ReCommentRepository;
import ssafy.study.backend.domain.edu.post.entity.Post;
import ssafy.study.backend.domain.edu.post.repository.PostRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.fixture.CommentFixture;
import ssafy.study.backend.fixture.CurriculumFixture;
import ssafy.study.backend.fixture.MemberFixture;
import ssafy.study.backend.fixture.PostFixture;
import ssafy.study.backend.fixture.StudyFixture;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

	@InjectMocks
	private CommentService commentService;

	@Mock
	private EntityManager entityManager;

	@Mock
	private PostRepository postRepository;

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private ReCommentRepository reCommentRepository;

	// ===================== 댓글 생성 =====================

	@Test
	@DisplayName("댓글 생성 성공")
	void 댓글_생성_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Post post = PostFixture.post(1L, author, CurriculumFixture.curriculum(1L, StudyFixture.study(1L)));
		CommentRequest request = new CommentRequest("댓글 내용");

		given(entityManager.getReference(Member.class, 1L)).willReturn(author);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(commentRepository.save(any(Comment.class))).willReturn(CommentFixture.comment(1L, author, post));

		// when & then
		assertThatNoException().isThrownBy(() -> commentService.createComment(1L, 1L, request));
		then(commentRepository).should().save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 생성 실패 - 존재하지 않는 게시글")
	void 댓글_생성_실패_존재하지_않는_게시글() {
		// given
		Member author = MemberFixture.member(1L);
		CommentRequest request = new CommentRequest("댓글 내용");

		given(entityManager.getReference(Member.class, 1L)).willReturn(author);
		given(postRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.createComment(1L, 999L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
	}

	// ===================== 댓글 목록 조회 =====================

	@Test
	@DisplayName("게시글별 댓글 목록 조회 성공")
	void 게시글별_댓글_목록_조회_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Post post = PostFixture.post(1L, author, CurriculumFixture.curriculum(1L, StudyFixture.study(1L)));
		Comment comment1 = CommentFixture.comment(1L, author, post);
		Comment comment2 = CommentFixture.comment(2L, author, post);
		PageRequest pageable = PageRequest.of(0, 10);
		Page<Comment> commentPage = new PageImpl<>(List.of(comment1, comment2), pageable, 2);

		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(commentRepository.findByPostId(1L, pageable)).willReturn(commentPage);

		// when
		Page<CommentResponse> result = commentService.getCommentsByPostId(1L, pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getContent()).hasSize(2);
	}

	@Test
	@DisplayName("게시글별 댓글 목록 조회 실패 - 존재하지 않는 게시글")
	void 게시글별_댓글_목록_조회_실패_존재하지_않는_게시글() {
		// given
		PageRequest pageable = PageRequest.of(0, 10);
		given(postRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.getCommentsByPostId(999L, pageable))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
	}

	// ===================== 댓글 수정 =====================

	@Test
	@DisplayName("댓글 수정 성공")
	void 댓글_수정_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Post post = PostFixture.post(1L, author, CurriculumFixture.curriculum(1L, StudyFixture.study(1L)));
		Comment comment = CommentFixture.comment(1L, author, post);
		CommentRequest request = new CommentRequest("수정된 댓글 내용");

		given(commentRepository.findCommentById(1L)).willReturn(Optional.of(comment));

		// when
		CommentResponse result = commentService.updateComment(1L, 1L, request);

		// then
		assertThat(result.content()).isEqualTo("수정된 댓글 내용");
		assertThat(result.commentId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("댓글 수정 실패 - 존재하지 않는 댓글")
	void 댓글_수정_실패_존재하지_않는_댓글() {
		// given
		CommentRequest request = new CommentRequest("수정된 댓글 내용");
		given(commentRepository.findCommentById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.updateComment(1L, 999L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
	}

	@Test
	@DisplayName("댓글 수정 실패 - 작성자가 아닌 경우")
	void 댓글_수정_실패_작성자가_아닌_경우() {
		// given
		Member author = MemberFixture.member(1L);
		Post post = PostFixture.post(1L, author, CurriculumFixture.curriculum(1L, StudyFixture.study(1L)));
		Comment comment = CommentFixture.comment(1L, author, post);
		CommentRequest request = new CommentRequest("수정된 댓글 내용");

		given(commentRepository.findCommentById(1L)).willReturn(Optional.of(comment));

		// when & then (authorId가 다른 2L로 요청)
		assertThatThrownBy(() -> commentService.updateComment(2L, 1L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_ACCESS_DENIED);
	}

	// ===================== 댓글 삭제 =====================

	@Test
	@DisplayName("댓글 삭제 성공")
	void 댓글_삭제_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Post post = PostFixture.post(1L, author, CurriculumFixture.curriculum(1L, StudyFixture.study(1L)));
		Comment comment = CommentFixture.comment(1L, author, post);

		given(commentRepository.findCommentById(1L)).willReturn(Optional.of(comment));
		willDoNothing().given(commentRepository).delete(comment);

		// when & then
		assertThatNoException().isThrownBy(() -> commentService.deleteComment(1L, 1L));
		then(commentRepository).should().delete(comment);
	}

	@Test
	@DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
	void 댓글_삭제_실패_존재하지_않는_댓글() {
		// given
		given(commentRepository.findCommentById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.deleteComment(1L, 999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
	}

	@Test
	@DisplayName("댓글 삭제 실패 - 작성자가 아닌 경우")
	void 댓글_삭제_실패_작성자가_아닌_경우() {
		// given
		Member author = MemberFixture.member(1L);
		Post post = PostFixture.post(1L, author, CurriculumFixture.curriculum(1L, StudyFixture.study(1L)));
		Comment comment = CommentFixture.comment(1L, author, post);

		given(commentRepository.findCommentById(1L)).willReturn(Optional.of(comment));

		// when & then (authorId가 다른 2L로 요청)
		assertThatThrownBy(() -> commentService.deleteComment(2L, 1L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_ACCESS_DENIED);
	}

	// ===================== 대댓글 생성 =====================

	@Test
	@DisplayName("대댓글 생성 성공")
	void 대댓글_생성_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Post post = PostFixture.post(1L, author, CurriculumFixture.curriculum(1L, StudyFixture.study(1L)));
		Comment comment = CommentFixture.comment(1L, author, post);
		CommentRequest request = new CommentRequest("대댓글 내용");

		given(entityManager.getReference(Member.class, 1L)).willReturn(author);
		given(commentRepository.findCommentById(1L)).willReturn(Optional.of(comment));
		given(reCommentRepository.save(any(ReComment.class))).willReturn(CommentFixture.reComment(1L, author, comment));

		// when
		assertThatNoException().isThrownBy(() -> commentService.createReComment(1L, 1L, request));

		// then
		then(reCommentRepository).should().save(any(ReComment.class));
		assertThat(comment.getReCommentCount()).isEqualTo(1L);
	}

	@Test
	@DisplayName("대댓글 생성 실패 - 존재하지 않는 댓글")
	void 대댓글_생성_실패_존재하지_않는_댓글() {
		// given
		Member author = MemberFixture.member(1L);
		CommentRequest request = new CommentRequest("대댓글 내용");

		given(entityManager.getReference(Member.class, 1L)).willReturn(author);
		given(commentRepository.findCommentById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.createReComment(1L, 999L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
	}

	// ===================== 대댓글 목록 조회 =====================

	@Test
	@DisplayName("댓글별 대댓글 목록 조회 성공")
	void 댓글별_대댓글_목록_조회_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Post post = PostFixture.post(1L, author, CurriculumFixture.curriculum(1L, StudyFixture.study(1L)));
		Comment comment = CommentFixture.comment(1L, author, post);
		ReComment reComment1 = CommentFixture.reComment(1L, author, comment);
		ReComment reComment2 = CommentFixture.reComment(2L, author, comment);
		PageRequest pageable = PageRequest.of(0, 10);
		Page<ReComment> reCommentPage = new PageImpl<>(List.of(reComment1, reComment2), pageable, 2);

		given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
		given(reCommentRepository.findByCommentId(1L, pageable)).willReturn(reCommentPage);

		// when
		Page<ReCommentResponse> result = commentService.getReCommentsByCommentId(1L, pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getContent()).hasSize(2);
	}

	@Test
	@DisplayName("댓글별 대댓글 목록 조회 실패 - 존재하지 않는 댓글")
	void 댓글별_대댓글_목록_조회_실패_존재하지_않는_댓글() {
		// given
		PageRequest pageable = PageRequest.of(0, 10);
		given(commentRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.getReCommentsByCommentId(999L, pageable))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
	}

	// ===================== 대댓글 수정 =====================

	@Test
	@DisplayName("대댓글 수정 성공")
	void 대댓글_수정_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Post post = PostFixture.post(1L, author, CurriculumFixture.curriculum(1L, StudyFixture.study(1L)));
		Comment comment = CommentFixture.comment(1L, author, post);
		ReComment reComment = CommentFixture.reComment(1L, author, comment);
		CommentRequest request = new CommentRequest("수정된 대댓글 내용");

		given(reCommentRepository.findReCommentById(1L)).willReturn(Optional.of(reComment));

		// when
		ReCommentResponse result = commentService.updateReComment(1L, 1L, request);

		// then
		assertThat(result.content()).isEqualTo("수정된 대댓글 내용");
		assertThat(result.reCommentId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("대댓글 수정 실패 - 존재하지 않는 대댓글")
	void 대댓글_수정_실패_존재하지_않는_대댓글() {
		// given
		CommentRequest request = new CommentRequest("수정된 대댓글 내용");
		given(reCommentRepository.findReCommentById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.updateReComment(1L, 999L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECOMMENT_NOT_FOUND);
	}

	@Test
	@DisplayName("대댓글 수정 실패 - 작성자가 아닌 경우")
	void 대댓글_수정_실패_작성자가_아닌_경우() {
		// given
		Member author = MemberFixture.member(1L);
		Post post = PostFixture.post(1L, author, CurriculumFixture.curriculum(1L, StudyFixture.study(1L)));
		Comment comment = CommentFixture.comment(1L, author, post);
		ReComment reComment = CommentFixture.reComment(1L, author, comment);
		CommentRequest request = new CommentRequest("수정된 대댓글 내용");

		given(reCommentRepository.findReCommentById(1L)).willReturn(Optional.of(reComment));

		// when & then (authorId가 다른 2L로 요청)
		assertThatThrownBy(() -> commentService.updateReComment(2L, 1L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_ACCESS_DENIED);
	}

	// ===================== 대댓글 삭제 =====================

	@Test
	@DisplayName("대댓글 삭제 성공")
	void 대댓글_삭제_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Post post = PostFixture.post(1L, author, CurriculumFixture.curriculum(1L, StudyFixture.study(1L)));
		Comment comment = CommentFixture.comment(1L, author, post);
		comment.incrementReCommentCount();
		ReComment reComment = CommentFixture.reComment(1L, author, comment);

		given(reCommentRepository.findReCommentById(1L)).willReturn(Optional.of(reComment));
		given(commentRepository.findCommentById(1L)).willReturn(Optional.of(comment));
		willDoNothing().given(reCommentRepository).delete(reComment);

		// when
		assertThatNoException().isThrownBy(() -> commentService.deleteReComment(1L, 1L));

		// then
		then(reCommentRepository).should().delete(reComment);
		assertThat(comment.getReCommentCount()).isEqualTo(0L);
	}

	@Test
	@DisplayName("대댓글 삭제 실패 - 존재하지 않는 대댓글")
	void 대댓글_삭제_실패_존재하지_않는_대댓글() {
		// given
		given(reCommentRepository.findReCommentById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.deleteReComment(1L, 999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECOMMENT_NOT_FOUND);
	}

	@Test
	@DisplayName("대댓글 삭제 실패 - 작성자가 아닌 경우")
	void 대댓글_삭제_실패_작성자가_아닌_경우() {
		// given
		Member author = MemberFixture.member(1L);
		Post post = PostFixture.post(1L, author, CurriculumFixture.curriculum(1L, StudyFixture.study(1L)));
		Comment comment = CommentFixture.comment(1L, author, post);
		ReComment reComment = CommentFixture.reComment(1L, author, comment);

		given(reCommentRepository.findReCommentById(1L)).willReturn(Optional.of(reComment));

		// when & then (authorId가 다른 2L로 요청)
		assertThatThrownBy(() -> commentService.deleteReComment(2L, 1L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_ACCESS_DENIED);
	}
}
