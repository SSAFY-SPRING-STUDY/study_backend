package ssafy.study.backend.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.auth.controller.dto.LoginRequest;
import ssafy.study.backend.domain.auth.service.AuthService;
import ssafy.study.backend.domain.auth.service.dto.AuthResult;
import ssafy.study.backend.global.cookie.CookieService;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

	private final AuthService authService;
	private final CookieService cookieService;

	/**
	 * 로그인
	 */
	@PostMapping(value = "/login", consumes = "application/json")
	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> login(
		@Valid @RequestBody LoginRequest request,
		HttpServletResponse response
	) {
		AuthResult result = authService.login(request);

		cookieService.setAccessToken(response, result.accessToken());
		cookieService.setRefreshToken(response, result.refreshToken());

		return ApiResponse.success("로그인이 성공적으로 완료되었습니다.");
	}

	/**
	 * 토큰 재발급 (access + refresh 로테이션)
	 */
	@PostMapping("/refresh")
	@Operation(summary = "토큰 재발급", description = "RefreshToken을 이용해 AccessToken과 RefreshToken을 재발급합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> refresh(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String refreshToken = cookieService.extractRefreshToken(request);

		if (refreshToken == null) {
			throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
		}

		AuthResult result = authService.reissue(refreshToken);

		cookieService.setAccessToken(response, result.accessToken());
		cookieService.setRefreshToken(response, result.refreshToken());

		return ApiResponse.success("토큰이 재발급되었습니다.");
	}

	/**
	 * 로그아웃
	 */
	@PostMapping("/logout")
	@Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> logout(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String refreshToken = cookieService.extractRefreshToken(request);

		if (refreshToken != null) {
			authService.deleteRefreshToken(refreshToken);
		}

		cookieService.deleteAccessToken(response);
		cookieService.deleteRefreshToken(response);

		return ApiResponse.success("로그아웃이 완료되었습니다.");
	}
}