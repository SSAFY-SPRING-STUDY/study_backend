package ssafy.study.backend.domain.edu.notice.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.notice.controller.dto.request.NoticeRequest;
import ssafy.study.backend.domain.edu.notice.controller.dto.response.NoticeResponse;
import ssafy.study.backend.domain.edu.notice.service.NoticeService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name="공지", description = "공지 관련 API")
public class NoticeController {
	private final NoticeService noticeService;

	@GetMapping("/notices")
	@Operation(summary = "공지사항 목록 조회", description = "모든 공지사항의 목록을 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Page<NoticeResponse>> getNotices(
		@RequestParam int page,
		@RequestParam int size
	) {
		Page<NoticeResponse> response = noticeService.getNotices(PageRequest.of(page, size));
		return ApiResponse.success("공지사항 목록이 성공적으로 조회되었습니다.", response);
	}

	@GetMapping("/notices/{noticeId}")
	@Operation(summary = "특정 공지사항 조회", description = "특정 공지사항을 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<NoticeResponse> getNotice(
		@PathVariable Long noticeId
	) {
		NoticeResponse response = noticeService.getNotice(noticeId);
		return ApiResponse.success("공지사항이 성공적으로 조회되었습니다.", response);
	}

	@PostMapping("/notices")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "공지사항 생성", description = "새로운 공지사항을 생성합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<NoticeResponse> createNotice(
		@Valid @RequestBody NoticeRequest request,
		@AuthenticationPrincipal Long authorId
	) {
		noticeService.createNotice(request, authorId);
		return ApiResponse.success("공지사항이 성공적으로 생성되었습니다.", null);
	}

	@PutMapping("/notices/{noticeId}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "공지사항 수정", description = "특정 공지사항을 수정합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<NoticeResponse> updateNotice(
		@PathVariable Long noticeId,
		@Valid @RequestBody NoticeRequest request,
		@AuthenticationPrincipal Long authorId
	) {
		NoticeResponse response = noticeService.updateNotice(noticeId, authorId, request);
		return ApiResponse.success("공지사항이 성공적으로 수정되었습니다.", response);
	}

	@DeleteMapping("/notices/{noticeId}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "공지사항 삭제", description = "특정 공지사항을 삭제합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> deleteNotice(
		@PathVariable Long noticeId,
		@AuthenticationPrincipal Long authorId
	) {
		noticeService.deleteNotice(noticeId, authorId);
		return ApiResponse.success("공지사항이 성공적으로 삭제되었습니다.", null);
	}
}
