package ssafy.study.backend.domain.member.controller;

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
import ssafy.study.backend.domain.member.controller.dto.request.ConfirmProfileImageRequest;
import ssafy.study.backend.domain.member.controller.dto.request.MemberUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.request.PasswordUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.request.ProfileImagePresignedUrlRequest;
import ssafy.study.backend.domain.member.controller.dto.request.SignupRequest;
import ssafy.study.backend.domain.member.controller.dto.request.UpdateDescriptionRequest;
import ssafy.study.backend.domain.member.controller.dto.response.MemberDetailInfo;
import ssafy.study.backend.domain.member.controller.dto.response.MemberInfo;
import ssafy.study.backend.domain.member.controller.dto.response.ProfileImagePresignedUrlResponse;
import ssafy.study.backend.domain.member.service.MemberService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {
	private final MemberService memberService;

	@PostMapping(value = "/signup", consumes = "application/json")
	@Operation(summary = "회원가입", description = "회원가입을 완료합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Void> join(@Valid @RequestBody SignupRequest request) {
		memberService.signup(request);
		return ApiResponse.success("회원가입이 성공적으로 완료되었습니다.");
	}

	@GetMapping("/me")
	@Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<MemberDetailInfo> getMyInfo(@AuthenticationPrincipal Long memberId) {
		MemberDetailInfo memberDetailInfo = memberService.getDetailInfo(memberId);
		return ApiResponse.success("내 정보 조회가 성공적으로 완료되었습니다.", memberDetailInfo);
	}

	@GetMapping("/{memberId}")
	@Operation(summary = "회원 정보 조회", description = "특정 회원의 정보를 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<MemberDetailInfo> getMemberInfo(@PathVariable Long memberId) {
		MemberDetailInfo memberDetailInfo = memberService.getDetailInfo(memberId);
		return ApiResponse.success("회원 정보 조회가 성공적으로 완료되었습니다.", memberDetailInfo);
	}

	@PatchMapping("/me")
	@Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<MemberInfo> updateMyInfo(@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody MemberUpdateRequest request) {
		MemberInfo memberInfo = memberService.updateInfo(memberId, request);
		return ApiResponse.success("내 정보 수정이 성공적으로 완료되었습니다.", memberInfo);
	}

	@PatchMapping("/me/profile/description")
	@Operation(summary = "자기소개 수정", description = "현재 로그인한 사용자의 자기소개를 수정합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<MemberDetailInfo> updateDescription(@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody UpdateDescriptionRequest request) {
		MemberDetailInfo memberDetailInfo = memberService.updateDescription(memberId, request);
		return ApiResponse.success("자기소개 수정이 성공적으로 완료되었습니다.", memberDetailInfo);
	}

	@PostMapping("/me/profile/image/presigned-url")
	@Operation(summary = "프로필 이미지 업로드 URL 발급", description = "S3에 직접 업로드할 Presigned URL을 발급합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ProfileImagePresignedUrlResponse> getProfileImagePresignedUrl(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody ProfileImagePresignedUrlRequest request) {
		ProfileImagePresignedUrlResponse response = memberService.getProfileImagePresignedUrl(memberId, request);
		return ApiResponse.success("프로필 이미지 업로드 URL 발급이 성공적으로 완료되었습니다.", response);
	}

	@PatchMapping("/me/profile/image")
	@Operation(summary = "프로필 이미지 수정", description = "S3 업로드 완료 후 프로필 이미지를 등록합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<MemberDetailInfo> confirmProfileImage(@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody ConfirmProfileImageRequest request) {
		MemberDetailInfo memberDetailInfo = memberService.confirmProfileImage(memberId, request);
		return ApiResponse.success("프로필 이미지 수정이 성공적으로 완료되었습니다.", memberDetailInfo);
	}

	@DeleteMapping("/me/profile/image")
	@Operation(summary = "프로필 이미지 삭제", description = "현재 로그인한 사용자의 프로필 이미지를 삭제합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<MemberDetailInfo> deleteProfileImage(@AuthenticationPrincipal Long memberId) {
		MemberDetailInfo memberDetailInfo = memberService.deleteProfileImage(memberId);
		return ApiResponse.success("프로필 이미지 삭제가 성공적으로 완료되었습니다.", memberDetailInfo);
	}

	@PatchMapping("/me/password")
	@Operation(summary = "내 비밀번호 수정", description = "현재 로그인한 사용자의 비밀번호를 수정합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> updateMyPassword(@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody PasswordUpdateRequest request) {
		memberService.updatePassword(memberId, request);
		return ApiResponse.success("내 비밀번호 수정이 성공적으로 완료되었습니다.");
	}
}
