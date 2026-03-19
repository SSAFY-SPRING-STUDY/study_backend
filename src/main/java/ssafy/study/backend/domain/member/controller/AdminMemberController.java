package ssafy.study.backend.domain.member.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.member.controller.dto.request.AdminMemberUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.response.MemberInfo;
import ssafy.study.backend.domain.member.service.AdminMemberService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Member", description = "관리자 회원 관리 API")
public class AdminMemberController {

	private final AdminMemberService adminMemberService;

	@GetMapping
	@Operation(summary = "회원 목록 조회", description = "키워드(이메일/닉네임)로 회원을 검색합니다. keyword가 없으면 전체 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Page<MemberInfo>> getMembers(
		@RequestParam(required = false) String keyword,
		@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Page<MemberInfo> members = adminMemberService.getMembers(keyword, pageable);
		return ApiResponse.success("회원 목록 조회가 완료되었습니다.", members);
	}

	@GetMapping("/{memberId}")
	@Operation(summary = "회원 상세 조회", description = "특정 회원의 정보를 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<MemberInfo> getMember(@PathVariable Long memberId) {
		MemberInfo member = adminMemberService.getMember(memberId);
		return ApiResponse.success("회원 상세 조회가 완료되었습니다.", member);
	}

	@PatchMapping("/{memberId}")
	@Operation(summary = "회원 정보 수정", description = "회원의 이름, 닉네임, 역할, 레벨을 수정합니다. null 필드는 수정하지 않습니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<MemberInfo> updateMember(
		@PathVariable Long memberId,
		@Valid @RequestBody AdminMemberUpdateRequest request
	) {
		MemberInfo member = adminMemberService.updateMember(memberId, request);
		return ApiResponse.success("회원 정보 수정이 완료되었습니다.", member);
	}
}
