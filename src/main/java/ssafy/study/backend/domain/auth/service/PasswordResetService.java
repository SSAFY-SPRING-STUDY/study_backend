package ssafy.study.backend.domain.auth.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ssafy.study.backend.domain.auth.controller.dto.PasswordResetConfirmRequest;
import ssafy.study.backend.domain.auth.controller.dto.PasswordResetRequestRequest;
import ssafy.study.backend.domain.auth.repository.PasswordResetRateLimiter;
import ssafy.study.backend.domain.auth.repository.PasswordResetTokenRepository;
import ssafy.study.backend.domain.auth.repository.RefreshTokenRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.global.config.FrontProperties;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;
import ssafy.study.backend.global.mail.MailSender;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

	private static final String PASSWORD_RESET_PATH = "/password-reset/confirm";

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final PasswordResetTokenRepository tokenRepository;
	private final PasswordResetRateLimiter rateLimiter;
	private final RefreshTokenRepository refreshTokenRepository;
	private final MailSender mailSender;
	private final FrontProperties frontProperties;

	/**
	 * 비밀번호 재설정 요청.
	 * - 회원 존재 여부와 무관하게 동일하게 동작 (User Enumeration 방지).
	 * - 실제 메일은 가입된 회원에게만 발송.
	 * - 메일 발송은 @Async라 즉시 반환.
	 */
	public void requestReset(PasswordResetRequestRequest request) {
		String email = request.email();

		if (!rateLimiter.tryAcquire(email)) {
			log.warn("Password reset rate limit exceeded for email={}", email);
			return;
		}

		Optional<Member> memberOpt = memberRepository.findByEmail(email);
		if (memberOpt.isEmpty()) {
			log.info("Password reset requested for non-existent email={}", email);
			return;
		}

		Member member = memberOpt.get();

		if (member.getPassword() == null) {
			sendGithubOnlyMail(email);
			return;
		}

		String token = UUID.randomUUID().toString();
		tokenRepository.save(token, member.getId());
		sendResetMail(email, token);
	}

	/**
	 * 비밀번호 재설정 확정.
	 * - 토큰을 즉시 삭제하여 재사용 차단.
	 * - 비밀번호 변경 + RefreshToken 무효화 + 알림 메일 발송.
	 */
	@Transactional
	public void confirmReset(PasswordResetConfirmRequest request) {
		Long memberId = tokenRepository.findMemberIdAndDelete(request.token());
		if (memberId == null) {
			throw new CustomException(ErrorCode.INVALID_PASSWORD_RESET_TOKEN);
		}

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_PASSWORD_RESET_TOKEN));

		member.setPassword(passwordEncoder.encode(request.newPassword()));
		refreshTokenRepository.delete(memberId);

		sendResetSuccessMail(member.getEmail());
	}

	// ===== private: 메일 발송 =====

	private void sendResetMail(String email, String token) {
		String resetUrl = frontProperties.baseUrl() + PASSWORD_RESET_PATH + "?token=" + token;
		String subject = "[SSAFY Study] 비밀번호 재설정 안내";
		String body = """
			안녕하세요.

			비밀번호 재설정 요청을 받았습니다.
			아래 링크를 클릭하여 새 비밀번호를 설정해주세요.

			%s

			본 링크는 30분간 유효합니다.

			본인이 요청하지 않은 경우 이 메일을 무시해주세요.
			계정에는 아무런 변경도 일어나지 않습니다.
			""".formatted(resetUrl);
		mailSender.send(email, subject, body);
	}

	private void sendGithubOnlyMail(String email) {
		String subject = "[SSAFY Study] 비밀번호 재설정 안내";
		String body = """
			안녕하세요.

			비밀번호 재설정 요청을 받았으나, 해당 계정은 GitHub 로그인으로 가입되어 있어
			비밀번호 재설정이 불가능합니다.

			GitHub 로그인을 이용해주세요.
			""";
		mailSender.send(email, subject, body);
	}

	private void sendResetSuccessMail(String email) {
		String subject = "[SSAFY Study] 비밀번호가 변경되었습니다";
		String body = """
			안녕하세요.

			회원님의 비밀번호가 방금 변경되었습니다.
			모든 기기에서 자동으로 로그아웃되었습니다.

			본인이 변경한 것이 맞다면 이 메일을 무시해주세요.
			변경하지 않으셨다면, 즉시 고객센터로 문의해주세요.
			""";
		mailSender.send(email, subject, body);
	}
}
