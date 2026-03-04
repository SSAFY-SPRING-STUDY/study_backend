package ssafy.study.backend.domain.study.post.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.study.post.controller.dto.request.PostCreateRequest;
import ssafy.study.backend.domain.study.post.controller.dto.response.PostDetailResponse;
import ssafy.study.backend.domain.study.post.controller.dto.response.PostSimpleResponse;
import ssafy.study.backend.domain.study.post.service.PostService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name="Post", description = "게시글 관련 API")
public class PostController {
	private final PostService postService;

	@PostMapping("/curriculums/{curriculumId}/posts")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<PostSimpleResponse> create(
		@AuthenticationPrincipal Long authorId,
		@PathVariable Long curriculumId, @Valid @RequestBody PostCreateRequest request) {

		PostSimpleResponse response = postService.createPost(authorId, curriculumId, request);

		return ApiResponse.success("게시글이 성공적으로 생성되었습니다.", response);
	}

	@GetMapping(value = "/posts/{postId}")
	@Operation(summary = "게시글 조회", description = "특정 게시글의 정보를 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<PostDetailResponse> read(@PathVariable Long postId) {
		PostDetailResponse response = postService.getPost(postId);
		return ApiResponse.success("게시글이 성공적으로 조회되었습니다.", response);
	}

	@PutMapping("/posts/{postId}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "게시글 수정", description = "특정 게시글을 수정합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<PostSimpleResponse> update(
		@AuthenticationPrincipal Long authorId,
		@PathVariable Long postId,
		@Valid @RequestBody PostCreateRequest request
	) {
		PostSimpleResponse response = postService.updatePost(authorId, postId, request);
		return ApiResponse.success("게시글이 성공적으로 수정되었습니다.", response);
	}

	@DeleteMapping("/posts/{postId}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> delete(
		@AuthenticationPrincipal Long authorId,
		@PathVariable Long postId
	) {
		postService.deletePost(authorId, postId);
		return ApiResponse.success("게시글이 성공적으로 삭제되었습니다.");
	}


}
