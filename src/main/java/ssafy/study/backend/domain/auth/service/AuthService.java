package ssafy.study.backend.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.auth.controller.dto.LoginRequest;
import ssafy.study.backend.domain.auth.repository.RefreshTokenRepository;
import ssafy.study.backend.domain.auth.service.dto.AuthResult;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.entity.MemberRole;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;
import ssafy.study.backend.global.security.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	/* =========================
	   로그인
	   ========================= */

	@Transactional
	public AuthResult login(LoginRequest request) {
		Member member = memberRepository.findByEmail(request.email())
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new CustomException(ErrorCode.BAD_CREDENTIAL);
		}

		String accessToken = jwtProvider.generateAccessToken(member.getId(), member.getRole());
		String refreshToken = jwtProvider.generateRefreshToken(member.getId(), member.getRole());

		refreshTokenRepository.save(member.getId(), refreshToken);

		return new AuthResult(accessToken, refreshToken);
	}

	/* =========================
	   토큰 재발급 (access + refresh 로테이션)
	   ========================= */

	public AuthResult reissue(String refreshToken) {
		Claims claims = jwtProvider.validateRefreshToken(refreshToken);
		Long memberId = Long.valueOf(claims.getSubject());

		String savedToken = refreshTokenRepository.findByMemberId(memberId);
		if (savedToken == null || !savedToken.equals(refreshToken)) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}

		MemberRole role = MemberRole.valueOf(claims.get("role", String.class));

		String newAccessToken = jwtProvider.generateAccessToken(memberId, role);
		String newRefreshToken = jwtProvider.generateRefreshToken(memberId, role);

		refreshTokenRepository.save(memberId, newRefreshToken);

		return new AuthResult(newAccessToken, newRefreshToken);
	}

	/* =========================
	   로그아웃
	   ========================= */

	public void deleteRefreshToken(String refreshToken) {
		// 만료된 토큰도 로그아웃 가능하도록 expiry 무시하고 memberId 추출
		Long memberId = jwtProvider.extractMemberIdIgnoreExpiry(refreshToken);
		refreshTokenRepository.delete(memberId);
	}
}