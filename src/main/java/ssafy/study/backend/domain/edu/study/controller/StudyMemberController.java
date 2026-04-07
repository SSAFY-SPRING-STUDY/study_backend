package ssafy.study.backend.domain.edu.study.controller;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.study.controller.dto.request.RoleChangeRequest;
import ssafy.study.backend.domain.edu.study.controller.dto.response.StudyMemberListResponse;
import ssafy.study.backend.domain.edu.study.service.StudyMemberService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/studies/{studyId}/members")
@RequiredArgsConstructor
@Tag(name = "StudyMember", description = "스터디 멤버 관련 API")
public class StudyMemberController {

	private final StudyMemberService studyMemberService;

	@PostMapping
	@Operation(summary = "스터디 참여", description = "스터디에 참여합니다. GitHub 계정 연동 필수.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Void> join(
		@PathVariable Long studyId,
		@AuthenticationPrincipal Long memberId
	) {
		studyMemberService.join(studyId, memberId);
		return ApiResponse.success("스터디에 참여했습니다.");
	}

	@GetMapping
	@Operation(summary = "스터디원 목록 조회", description = "스터디원 목록과 진행현황을 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<StudyMemberListResponse> getMembers(
		@PathVariable Long studyId
	) {
		StudyMemberListResponse response = studyMemberService.getMembers(studyId);
		return ApiResponse.success("스터디원 목록을 조회했습니다.", response);
	}

	@PatchMapping("/{targetMemberId}/role")
	@Operation(summary = "역할 변경", description = "스터디원의 역할을 변경합니다. LEADER 또는 ADMIN만 가능.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> changeRole(
		@PathVariable Long studyId,
		@PathVariable Long targetMemberId,
		@Valid @RequestBody RoleChangeRequest request,
		@AuthenticationPrincipal Long requesterId
	) {
		studyMemberService.changeRole(studyId, targetMemberId, request.role(), requesterId);
		return ApiResponse.success("역할이 변경되었습니다.");
	}

	@DeleteMapping("/me")
	@Operation(summary = "스터디 탈퇴", description = "스터디에서 탈퇴합니다. 연관된 과제 진행 기록이 함께 삭제됩니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> leave(
		@PathVariable Long studyId,
		@AuthenticationPrincipal Long memberId
	) {
		studyMemberService.leave(studyId, memberId);
		return ApiResponse.success("스터디에서 탈퇴했습니다.");
	}
}
