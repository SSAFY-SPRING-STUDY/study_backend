package ssafy.study.backend.domain.edu.comment.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.comment.controller.dto.request.CommentRequest;
import ssafy.study.backend.domain.edu.comment.controller.dto.response.CommentResponse;
import ssafy.study.backend.domain.edu.comment.controller.dto.response.ReCommentResponse;
import ssafy.study.backend.domain.edu.comment.service.CommentService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommentController {
	private final CommentService commentService;

	@PostMapping("/posts/{postId}/comments")
	@Operation(summary = "게시글 댓글 작성", description = "특정 게시글에 댓글을 작성합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Void> createComment(
		@PathVariable Long postId,
		@Valid @RequestBody CommentRequest request,
		@AuthenticationPrincipal Long authorId) {
		commentService.createComment(authorId, postId, request);
		return ApiResponse.success("댓글이 성공적으로 생성되었습니다.");
	}

	@GetMapping("/posts/{postId}/comments")
	@Operation(summary = "게시글 댓글 목록 조회", description = "특정 게시글의 최상위 댓글 목록을 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Page<CommentResponse>> getCommentsByPostId(
		@PathVariable Long postId,
		@PageableDefault Pageable pageable) {
		Page<CommentResponse> result = commentService.getCommentsByPostId(postId, pageable);
		return ApiResponse.success("댓글 목록이 성공적으로 조회되었습니다.", result);
	}

	@PatchMapping("/comments/{commentId}")
	@Operation(summary = "댓글 수정", description = "특정 댓글을 수정합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<CommentResponse> updateComment(
		@PathVariable Long commentId,
		@Valid @RequestBody CommentRequest request,
		@AuthenticationPrincipal Long authorId) {
		CommentResponse response = commentService.updateComment(authorId, commentId, request);
		return ApiResponse.success("댓글이 성공적으로 수정되었습니다.", response);
	}

	@DeleteMapping("/comments/{commentId}")
	@Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> deleteComment(
		@PathVariable Long commentId,
		@AuthenticationPrincipal Long authorId) {
		commentService.deleteComment(authorId, commentId);
		return ApiResponse.success("댓글이 성공적으로 삭제되었습니다.");
	}

	@PostMapping("/comments/{commentId}/recomments")
	@Operation(summary = "대댓글 작성", description = "특정 댓글에 대댓글을 작성합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Void> createReComment(
		@PathVariable Long commentId,
		@Valid @RequestBody CommentRequest request,
		@AuthenticationPrincipal Long authorId) {
		commentService.createReComment(authorId, commentId, request);
		return ApiResponse.success("대댓글이 성공적으로 생성되었습니다.");
	}

	@GetMapping("/comments/{commentId}/recomments")
	@Operation(summary = "대댓글 목록 조회", description = "특정 댓글의 대댓글 목록을 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Page<ReCommentResponse>> getReCommentsByCommentId(
		@PathVariable Long commentId,
		@PageableDefault Pageable pageable) {
		Page<ReCommentResponse> result = commentService.getReCommentsByCommentId(commentId, pageable);
		return ApiResponse.success("대댓글 목록이 성공적으로 조회되었습니다.", result);
	}

	@PatchMapping("/recomments/{reCommentId}")
	@Operation(summary = "대댓글 수정", description = "특정 대댓글을 수정합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ReCommentResponse> updateReComment(
		@PathVariable Long reCommentId,
		@Valid @RequestBody CommentRequest request,
		@AuthenticationPrincipal Long authorId) {
		ReCommentResponse response = commentService.updateReComment(authorId, reCommentId, request);
		return ApiResponse.success("대댓글이 성공적으로 수정되었습니다.", response);
	}

	@DeleteMapping("/recomments/{reCommentId}")
	@Operation(summary = "대댓글 삭제", description = "특정 대댓글을 삭제합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> deleteReComment(
		@PathVariable Long reCommentId,
		@AuthenticationPrincipal Long authorId) {
		commentService.deleteReComment(authorId, reCommentId);
		return ApiResponse.success("대댓글이 성공적으로 삭제되었습니다.");
	}
}
