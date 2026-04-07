package ssafy.study.backend.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ssafy.study.backend.domain.auth.controller.dto.LoginRequest;
import ssafy.study.backend.domain.auth.repository.RefreshTokenRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.fixture.MemberFixture;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;
import ssafy.study.backend.global.security.jwt.JwtProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	// ===== ISSUE-4: GitHub 전용 계정 일반 로그인 에러 =====

	@Test
	@DisplayName("GitHub 전용 계정으로 일반 로그인 시 GITHUB_ONLY_ACCOUNT 에러 반환")
	void GitHub전용_계정_일반_로그인_GITHUB_ONLY_ACCOUNT_에러() {
		// given
		Member githubOnlyMember = MemberFixture.githubOnlyMember(1L); // password = null
		LoginRequest request = new LoginRequest("github@github.noemail", "anyPassword");
		given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(githubOnlyMember));

		// when & then
		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.GITHUB_ONLY_ACCOUNT);
	}

	@Test
	@DisplayName("비밀번호가 틀린 경우 BAD_CREDENTIAL 에러 반환 (GitHub 계정 아님)")
	void 비밀번호_불일치_BAD_CREDENTIAL_에러() {
		// given
		Member member = MemberFixture.member(1L); // password = "encoded_password"
		LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
		given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
		given(passwordEncoder.matches("wrongPassword", member.getPassword())).willReturn(false);

		// when & then
		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_CREDENTIAL);
	}

	@Test
	@DisplayName("올바른 이메일과 비밀번호로 로그인 성공")
	void 일반_로그인_성공() {
		// given
		Member member = MemberFixture.member(1L);
		LoginRequest request = new LoginRequest("test@example.com", "password123");
		given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
		given(passwordEncoder.matches(request.password(), member.getPassword())).willReturn(true);
		given(jwtProvider.generateAccessToken(any(), any())).willReturn("access-token");
		given(jwtProvider.generateRefreshToken(any(), any())).willReturn("refresh-token");

		// when & then
		assertThatNoException().isThrownBy(() -> authService.login(request));
	}
}
