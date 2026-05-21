package ssafy.study.backend.domain.auth.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.auth.repository.RefreshTokenRepository;
import ssafy.study.backend.domain.auth.service.dto.AuthResult;
import ssafy.study.backend.domain.member.controller.dto.response.MemberInfo;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.entity.MemberLevel;
import ssafy.study.backend.domain.member.entity.MemberRole;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;
import ssafy.study.backend.global.github.GitHubOAuthClient;
import ssafy.study.backend.global.github.GitHubProperties;
import ssafy.study.backend.global.github.dto.GitHubTokenResponse;
import ssafy.study.backend.global.github.dto.GitHubUserResponse;
import ssafy.study.backend.global.security.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GitHubOAuthService {

	private final GitHubOAuthClient gitHubOAuthClient;
	private final GitHubProperties gitHubProperties;
	private final MemberRepository memberRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtProvider jwtProvider;

	/** GitHub 로그인 페이지 redirect URL 반환 */
	public String getLoginRedirectUrl() {
		return "https://github.com/login/oauth/authorize"
			+ "?client_id=" + gitHubProperties.clientId()
			+ "&redirect_uri=" + gitHubProperties.redirectUri()
			+ "&scope=read:user,user:email";
	}

	/** GitHub 로그인 콜백 처리 → AuthResult (JWT 포함) 반환 */
	@Transactional
	public AuthResult handleLoginCallback(String code) {
		GitHubTokenResponse tokenResponse;
		GitHubUserResponse userInfo;
		try {
			tokenResponse = gitHubOAuthClient.exchangeLoginCode(code);
			userInfo = gitHubOAuthClient.getUser(tokenResponse.accessToken());
		} catch (Exception e) {
			throw new CustomException(ErrorCode.GITHUB_OAUTH_FAILED);
		}

		Member member = memberRepository.findByGithubId(userInfo.id())
			.orElseGet(() -> resolveByEmailOrCreate(userInfo));

		String accessToken = jwtProvider.generateAccessToken(member.getId(), member.getRole());
		String refreshToken = jwtProvider.generateRefreshToken(member.getId(), member.getRole());
		refreshTokenRepository.save(member.getId(), refreshToken);

		return new AuthResult(MemberInfo.fromEntity(member), accessToken, refreshToken);
	}

	// ===== private =====

	private Member resolveByEmailOrCreate(GitHubUserResponse userInfo) {
		if (userInfo.email() != null) {
			return memberRepository.findByEmail(userInfo.email())
				.map(existing -> {
					existing.connectGithub(userInfo.id(), userInfo.login());
					return existing;
				})
				.orElseGet(() -> createGitHubMember(userInfo));
		}
		return createGitHubMember(userInfo);
	}

	private Member createGitHubMember(GitHubUserResponse userInfo) {
		String baseNickname = userInfo.login() != null ? userInfo.login() : "user";
		String nickname = generateUniqueNickname(baseNickname, userInfo.id());

		String email = (userInfo.email() != null && !userInfo.email().isBlank())
			? userInfo.email()
			: userInfo.login() + "@github.noemail";

		Member newMember = Member.builder()
			.name(userInfo.name() != null ? userInfo.name() : userInfo.login())
			.email(email)
			.nickname(nickname)
			.password(null)
			.role(MemberRole.ROLE_USER)
			.level(MemberLevel.BASIC)
			.githubId(userInfo.id())
			.githubUsername(userInfo.login())
			.build();

		return memberRepository.save(newMember);
	}

	private String generateUniqueNickname(String baseNickname, String githubId) {
		if (!memberRepository.existsByNickname(baseNickname)) {
			return baseNickname;
		}
		String combined = baseNickname + "_" + githubId;
		if (!memberRepository.existsByNickname(combined)) {
			return combined;
		}
		for (int i = 0; i < 3; i++) {
			String candidate = combined + "_" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
			if (!memberRepository.existsByNickname(candidate)) {
				return candidate;
			}
		}
		throw new CustomException(ErrorCode.USERNAME_DUPLICATE);
	}
}