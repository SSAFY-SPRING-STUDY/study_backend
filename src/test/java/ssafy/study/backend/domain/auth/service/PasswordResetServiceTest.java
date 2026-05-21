package ssafy.study.backend.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ssafy.study.backend.domain.auth.controller.dto.PasswordResetConfirmRequest;
import ssafy.study.backend.domain.auth.controller.dto.PasswordResetRequestRequest;
import ssafy.study.backend.domain.auth.repository.PasswordResetRateLimiter;
import ssafy.study.backend.domain.auth.repository.PasswordResetTokenRepository;
import ssafy.study.backend.domain.auth.repository.RefreshTokenRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.fixture.MemberFixture;
import ssafy.study.backend.global.config.FrontProperties;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;
import ssafy.study.backend.global.mail.MailSender;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

	@InjectMocks
	private PasswordResetService passwordResetService;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private PasswordResetTokenRepository tokenRepository;

	@Mock
	private PasswordResetRateLimiter rateLimiter;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private MailSender mailSender;

	@Mock
	private FrontProperties frontProperties;

	@BeforeEach
	void setUp() {
		// 기본적으로 rate limit 통과
		lenient().when(rateLimiter.tryAcquire(anyString())).thenReturn(true);
		lenient().when(frontProperties.baseUrl()).thenReturn("https://app.example.com");
	}

	// ===== requestReset =====

	@Test
	@DisplayName("정상 회원이 재설정 요청 시 토큰 저장 + 재설정 메일 발송")
	void 정상_회원_재설정_요청_토큰_저장_및_메일_발송() {
		// given
		Member member = MemberFixture.member(1L);
		given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));

		// when
		passwordResetService.requestReset(new PasswordResetRequestRequest("test@example.com"));

		// then
		then(tokenRepository).should().save(anyString(), eq(1L));
		then(mailSender).should().send(eq("test@example.com"), contains("비밀번호 재설정"), contains("https://app.example.com/password-reset/confirm?token="));
	}

	@Test
	@DisplayName("존재하지 않는 이메일은 토큰 저장도 메일 발송도 하지 않음")
	void 존재하지_않는_이메일_요청_메일_발송_안_함() {
		// given
		given(memberRepository.findByEmail("nobody@example.com")).willReturn(Optional.empty());

		// when
		passwordResetService.requestReset(new PasswordResetRequestRequest("nobody@example.com"));

		// then
		then(tokenRepository).shouldHaveNoInteractions();
		then(mailSender).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("GitHub OAuth 전용 계정 요청 시 안내 메일만 발송, 토큰 저장 안 함")
	void GitHub_전용_계정_요청_안내_메일_발송() {
		// given
		Member githubOnly = MemberFixture.githubOnlyMember(2L);
		given(memberRepository.findByEmail("github@github.noemail")).willReturn(Optional.of(githubOnly));

		// when
		passwordResetService.requestReset(new PasswordResetRequestRequest("github@github.noemail"));

		// then
		then(tokenRepository).shouldHaveNoInteractions();
		then(mailSender).should().send(eq("github@github.noemail"), anyString(), contains("GitHub 로그인"));
	}

	@Test
	@DisplayName("Rate limit 초과 시 회원 조회·메일 발송 모두 스킵")
	void Rate_limit_초과_시_메일_발송_안_함() {
		// given
		given(rateLimiter.tryAcquire("test@example.com")).willReturn(false);

		// when
		passwordResetService.requestReset(new PasswordResetRequestRequest("test@example.com"));

		// then
		then(memberRepository).shouldHaveNoInteractions();
		then(tokenRepository).shouldHaveNoInteractions();
		then(mailSender).shouldHaveNoInteractions();
	}

	// ===== confirmReset =====

	@Test
	@DisplayName("유효한 토큰으로 confirm 시 비밀번호 변경 + RefreshToken 삭제 + 알림 메일 발송")
	void 유효한_토큰_confirm_정상_동작() {
		// given
		String token = "valid-token";
		Member member = MemberFixture.member(1L);
		given(tokenRepository.findMemberIdAndDelete(token)).willReturn(1L);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(passwordEncoder.encode("newPassword123")).willReturn("encoded_new_password");

		// when
		passwordResetService.confirmReset(new PasswordResetConfirmRequest(token, "newPassword123"));

		// then
		assertThat(member.getPassword()).isEqualTo("encoded_new_password");
		then(refreshTokenRepository).should().delete(1L);
		then(mailSender).should().send(eq(member.getEmail()), contains("변경되었습니다"), anyString());
	}

	@Test
	@DisplayName("존재하지 않는 토큰으로 confirm 시 INVALID_PASSWORD_RESET_TOKEN 에러")
	void 존재하지_않는_토큰_confirm_에러() {
		// given
		given(tokenRepository.findMemberIdAndDelete("bad-token")).willReturn(null);

		// when & then
		assertThatThrownBy(() ->
			passwordResetService.confirmReset(new PasswordResetConfirmRequest("bad-token", "newPassword123"))
		)
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD_RESET_TOKEN);

		then(memberRepository).shouldHaveNoInteractions();
		then(refreshTokenRepository).shouldHaveNoInteractions();
		then(mailSender).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("토큰은 유효하나 회원이 삭제된 경우 INVALID_PASSWORD_RESET_TOKEN 에러")
	void 토큰_유효하나_회원_없음_에러() {
		// given
		given(tokenRepository.findMemberIdAndDelete("orphan-token")).willReturn(999L);
		given(memberRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() ->
			passwordResetService.confirmReset(new PasswordResetConfirmRequest("orphan-token", "newPassword123"))
		)
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD_RESET_TOKEN);
	}
}
