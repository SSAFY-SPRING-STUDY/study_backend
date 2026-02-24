package ssafy.study.backend.domain.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.member.controller.dto.request.SignupRequest;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.entity.MemberLevel;
import ssafy.study.backend.domain.member.entity.MemberRole;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public void signup(SignupRequest request) {
		if (memberRepository.existsByEmail(request.email())) {
			throw new CustomException(ErrorCode.EMAIL_DUPLICATE);
		}
		if (memberRepository.existsByNickname(request.nickname())) {
			throw new CustomException(ErrorCode.USERNAME_DUPLICATE);
		}

		Member member = Member.builder()
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.name(request.name())
			.nickname(request.nickname())
			.role(MemberRole.ROLE_USER)
			.level(MemberLevel.BASIC)
			.build();

		memberRepository.save(member);
	}
}