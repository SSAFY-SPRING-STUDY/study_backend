package ssafy.study.backend.domain.edu.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
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
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
	private final EntityManager entityManager;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final ReCommentRepository reCommentRepository;

	@Transactional
	public void createComment(Long authorId, Long postId, CommentRequest request) {
		Member author = entityManager.getReference(Member.class, authorId);
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		commentRepository.save(Comment.create(request.content(), author, post));
	}

	public Page<CommentResponse> getCommentsByPostId(Long postId, Pageable pageable) {
		postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		return commentRepository.findByPostId(postId, pageable)
			.map(CommentResponse::from);
	}

	@Transactional
	public CommentResponse updateComment(Long authorId, Long commentId, CommentRequest request) {
		Comment comment = commentRepository.findCommentById(commentId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

		if (!comment.getAuthor().getId().equals(authorId)) {
			throw new CustomException(ErrorCode.COMMENT_ACCESS_DENIED);
		}

		comment.updateContent(request.content());
		return CommentResponse.from(comment);
	}

	@Transactional
	public void deleteComment(Long authorId, Long commentId) {
		Comment comment = commentRepository.findCommentById(commentId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

		if (!comment.getAuthor().getId().equals(authorId)) {
			throw new CustomException(ErrorCode.COMMENT_ACCESS_DENIED);
		}

		commentRepository.delete(comment);
	}

	@Transactional
	public void createReComment(Long authorId, Long commentId, CommentRequest request) {
		Member author = entityManager.getReference(Member.class, authorId);
		Comment comment = commentRepository.findCommentById(commentId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

		reCommentRepository.save(ReComment.create(request.content(), author, comment));
		comment.incrementReCommentCount();
	}

	public Page<ReCommentResponse> getReCommentsByCommentId(Long commentId, Pageable pageable) {
		commentRepository.findById(commentId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

		return reCommentRepository.findByCommentId(commentId, pageable)
			.map(ReCommentResponse::from);
	}

	@Transactional
	public ReCommentResponse updateReComment(Long authorId, Long reCommentId, CommentRequest request) {
		ReComment reComment = reCommentRepository.findReCommentById(reCommentId)
			.orElseThrow(() -> new CustomException(ErrorCode.RECOMMENT_NOT_FOUND));

		if (!reComment.getAuthor().getId().equals(authorId)) {
			throw new CustomException(ErrorCode.COMMENT_ACCESS_DENIED);
		}

		reComment.updateContent(request.content());
		return ReCommentResponse.from(reComment);
	}

	@Transactional
	public void deleteReComment(Long authorId, Long reCommentId) {
		ReComment reComment = reCommentRepository.findReCommentById(reCommentId)
			.orElseThrow(() -> new CustomException(ErrorCode.RECOMMENT_NOT_FOUND));

		if (!reComment.getAuthor().getId().equals(authorId)) {
			throw new CustomException(ErrorCode.COMMENT_ACCESS_DENIED);
		}

		Comment comment = commentRepository.findCommentById(reComment.getComment().getId())
			.orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

		comment.decrementReCommentCount();
		reCommentRepository.delete(reComment);
	}
}
