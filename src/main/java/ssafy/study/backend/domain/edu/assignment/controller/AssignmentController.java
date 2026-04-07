package ssafy.study.backend.domain.edu.assignment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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
import ssafy.study.backend.domain.edu.assignment.controller.dto.request.AssignmentRequest;
import ssafy.study.backend.domain.edu.assignment.controller.dto.response.ApplyAssignmentResponse;
import ssafy.study.backend.domain.edu.assignment.controller.dto.response.AssignmentListItemResponse;
import ssafy.study.backend.domain.edu.assignment.controller.dto.response.AssignmentProgressResponse;
import ssafy.study.backend.domain.edu.assignment.controller.dto.response.AssignmentResponse;
import ssafy.study.backend.domain.edu.assignment.service.AssignmentService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "Assignment", description = "과제 관련 API")
public class AssignmentController {

	private final AssignmentService assignmentService;

	@PostMapping("/api/v1/studies/{studyId}/assignments")
	@Operation(summary = "과제 생성", description = "스터디에 과제를 생성합니다. LEADER 또는 ADMIN만 가능.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<AssignmentResponse> create(
		@PathVariable Long studyId,
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody AssignmentRequest request
	) {
		AssignmentResponse response = assignmentService.create(studyId, memberId, request);
		return ApiResponse.success("과제가 생성되었습니다.", response);
	}

	@GetMapping("/api/v1/studies/{studyId}/assignments")
	@Operation(summary = "과제 목록 조회", description = "스터디의 과제 목록을 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<List<AssignmentListItemResponse>> list(
		@PathVariable Long studyId,
		@AuthenticationPrincipal Long memberId
	) {
		List<AssignmentListItemResponse> response = assignmentService.list(studyId, memberId);
		return ApiResponse.success("과제 목록을 조회했습니다.", response);
	}

	@GetMapping("/api/v1/assignments/{assignmentId}")
	@Operation(summary = "과제 상세 조회", description = "과제 상세 정보를 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<AssignmentResponse> get(
		@PathVariable Long assignmentId,
		@AuthenticationPrincipal Long memberId
	) {
		AssignmentResponse response = assignmentService.get(assignmentId, memberId);
		return ApiResponse.success("과제를 조회했습니다.", response);
	}

	@PutMapping("/api/v1/assignments/{assignmentId}")
	@Operation(summary = "과제 수정", description = "과제를 수정합니다. LEADER 또는 ADMIN만 가능.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<AssignmentResponse> update(
		@PathVariable Long assignmentId,
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody AssignmentRequest request
	) {
		AssignmentResponse response = assignmentService.update(assignmentId, memberId, request);
		return ApiResponse.success("과제가 수정되었습니다.", response);
	}

	@DeleteMapping("/api/v1/assignments/{assignmentId}")
	@Operation(summary = "과제 삭제", description = "과제를 삭제합니다. LEADER 또는 ADMIN만 가능.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> delete(
		@PathVariable Long assignmentId,
		@AuthenticationPrincipal Long memberId
	) {
		assignmentService.delete(assignmentId, memberId);
		return ApiResponse.success("과제가 삭제되었습니다.");
	}

	@PostMapping("/api/v1/assignments/{assignmentId}/apply")
	@Operation(summary = "과제 신청", description = "과제를 신청합니다. GitHub Issue가 자동 생성됩니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ApplyAssignmentResponse> apply(
		@PathVariable Long assignmentId,
		@AuthenticationPrincipal Long memberId
	) {
		ApplyAssignmentResponse response = assignmentService.apply(assignmentId, memberId);
		return ApiResponse.success("과제 신청이 완료되었습니다.", response);
	}

	@GetMapping("/api/v1/studies/{studyId}/progress/me")
	@Operation(summary = "내 진행현황 조회", description = "스터디 내 내 과제별 상세 진행현황을 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<List<AssignmentProgressResponse>> getMyProgress(
		@PathVariable Long studyId,
		@AuthenticationPrincipal Long memberId
	) {
		List<AssignmentProgressResponse> response = assignmentService.getMyProgress(studyId, memberId);
		return ApiResponse.success("내 진행현황을 조회했습니다.", response);
	}
}
