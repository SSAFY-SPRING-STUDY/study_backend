package ssafy.study.backend.domain.member.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ssafy.study.backend.domain.member.service.MemberGithubConnectService;
import ssafy.study.backend.global.config.FrontProperties;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

/**
 * 로그인된 회원이 자기 계정에 GitHub 를 사후 연결할 때 사용하는 OAuth 흐름.
 * <p>
 * - GET /members/me/github/connect           : 인증 필수. GitHub OAuth 로 redirect.
 * - GET /members/me/github/connect/callback  : 익명 허용. state 로 회원 식별.
 *                                              완료 후 프론트 /members/me 로 redirect.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/members/me/github")
@RequiredArgsConstructor
@Tag(name = "Member GitHub Connect", description = "로그인된 사용자의 GitHub 계정 연결 흐름")
public class MemberGithubConnectController {

	private static final String FRONT_RESULT_PATH = "/members/me";

	private final MemberGithubConnectService connectService;
	private final FrontProperties frontProperties;

	@GetMapping("/connect")
	@Operation(summary = "GitHub 계정 연결 시작", description = "현재 로그인된 사용자의 계정에 GitHub 을 연결하기 위해 GitHub OAuth 페이지로 redirect 합니다.")
	public void startConnect(
		@AuthenticationPrincipal Long memberId,
		HttpServletResponse response
	) throws IOException {
		if (memberId == null) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}
		String redirectUrl = connectService.startConnect(memberId);
		response.sendRedirect(redirectUrl);
	}

	@GetMapping("/connect/callback")
	@Operation(summary = "GitHub 계정 연결 콜백", description = "GitHub 의 OAuth 콜백을 처리하고 프론트 /members/me 로 결과를 전달합니다.")
	public void handleCallback(
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String state,
		@RequestParam(name = "error", required = false) String oauthError,
		HttpServletResponse response
	) throws IOException {
		if (oauthError != null) {
			log.info("GitHub connect 흐름 사용자 거부: {}", oauthError);
			response.sendRedirect(buildResultUrl("error", "denied"));
			return;
		}
		if (code == null || state == null) {
			response.sendRedirect(buildResultUrl("error", "invalid_request"));
			return;
		}

		try {
			connectService.handleConnectCallback(code, state);
			response.sendRedirect(buildResultUrl("connected", null));
		} catch (CustomException e) {
			response.sendRedirect(buildResultUrl("error", reasonFor(e.getErrorCode())));
		} catch (Exception e) {
			log.error("GitHub connect 콜백 처리 중 예기치 못한 오류", e);
			response.sendRedirect(buildResultUrl("error", "internal"));
		}
	}

	private String buildResultUrl(String status, String reason) {
		StringBuilder sb = new StringBuilder(frontProperties.baseUrl())
			.append(FRONT_RESULT_PATH)
			.append("?github=")
			.append(URLEncoder.encode(status, StandardCharsets.UTF_8));
		if (reason != null) {
			sb.append("&reason=").append(URLEncoder.encode(reason, StandardCharsets.UTF_8));
		}
		return sb.toString();
	}

	private String reasonFor(ErrorCode code) {
		return switch (code) {
			case GITHUB_CONNECT_STATE_INVALID -> "state_invalid";
			case GITHUB_ALREADY_LINKED_TO_OTHER_MEMBER -> "already_used";
			case GITHUB_OAUTH_FAILED -> "oauth_failed";
			default -> "internal";
		};
	}
}
