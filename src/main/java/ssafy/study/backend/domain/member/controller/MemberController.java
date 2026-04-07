package ssafy.study.backend.domain.member.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.auth.service.GitHubOAuthService;
import ssafy.study.backend.domain.member.controller.dto.request.MemberUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.request.PasswordUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.request.SignupRequest;
import ssafy.study.backend.domain.member.controller.dto.response.MemberInfo;
import ssafy.study.backend.domain.member.service.MemberService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {
	private final MemberService memberService;
	private final GitHubOAuthService gitHubOAuthService;

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
	public ApiResponse<MemberInfo> getMyInfo(@AuthenticationPrincipal Long memberId) {
		MemberInfo memberInfo = memberService.getInfo(memberId);
		return ApiResponse.success("내 정보 조회가 성공적으로 완료되었습니다.", memberInfo);
	}

	@GetMapping("/{memberId}")
	@Operation(summary = "회원 정보 조회", description = "특정 회원의 정보를 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<MemberInfo> getMemberInfo(@PathVariable Long memberId) {
		MemberInfo memberInfo = memberService.getInfo(memberId);
		return ApiResponse.success("회원 정보 조회가 성공적으로 완료되었습니다.", memberInfo);
	}

	@PatchMapping("/me")
	@Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<MemberInfo> updateMyInfo(@AuthenticationPrincipal Long memberId, @Valid @RequestBody MemberUpdateRequest request
	) {
		MemberInfo memberInfo = memberService.updateInfo(memberId, request);
		return ApiResponse.success("내 정보 수정이 성공적으로 완료되었습니다.", memberInfo);
	}

	@GetMapping("/me/github/connect")
	@Operation(summary = "GitHub 계정 연결 시작", description = "GitHub OAuth 페이지로 redirect하여 계정 연결을 시작합니다.")
	public void startGithubConnect(
		@AuthenticationPrincipal Long memberId,
		HttpServletResponse response
	) throws IOException {
		response.sendRedirect(gitHubOAuthService.getConnectRedirectUrl(memberId));
	}

	@GetMapping("/me/github/connect/callback")
	@Operation(summary = "GitHub 계정 연결 콜백", description = "GitHub OAuth 콜백을 처리하고 계정을 연결합니다.")
	public void githubConnectCallback(
		@RequestParam String code,
		@RequestParam String state,
		HttpServletResponse response
	) throws IOException {
		String redirectUrl = gitHubOAuthService.handleConnectCallback(code, state);
		response.sendRedirect(redirectUrl);
	}

	@PatchMapping("/me/password")
	@Operation(summary = "내 비밀번호 수정", description = "현재 로그인한 사용자의 비밀번호를 수정합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> updateMyPassword(@AuthenticationPrincipal Long memberId, @Valid @RequestBody PasswordUpdateRequest request) {
		memberService.updatePassword(memberId, request);
		return ApiResponse.success("내 비밀번호 수정이 성공적으로 완료되었습니다.");
	}
}
