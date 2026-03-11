package ssafy.study.backend.domain.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.member.controller.dto.request.MemberUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.request.PasswordUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.request.SignupRequest;
import ssafy.study.backend.domain.member.controller.dto.response.MemberInfo;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.entity.MemberLevel;
import ssafy.study.backend.domain.member.entity.MemberRole;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
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

	public MemberInfo getInfo(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		return MemberInfo.fromEntity(member);
	}

	@Transactional
	public MemberInfo updateInfo(Long requesterId,MemberUpdateRequest request) {
		Member member = memberRepository.findById(requesterId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		if (request.nickname() != null && !request.nickname().equals(member.getNickname())) {
			if (memberRepository.existsByNickname(request.nickname())) {
				throw new CustomException(ErrorCode.USERNAME_DUPLICATE);
			}
			member.setNickname(request.nickname());
		}

		if (request.name() != null) {
			member.setName(request.name());
		}

		return MemberInfo.fromEntity(member);

	}

	@Transactional
	public void updatePassword(Long requesterId, PasswordUpdateRequest request) {
		Member member = memberRepository.findById(requesterId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		// 1. 기존 비밀번호가 맞는지 확인
		if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
			throw new CustomException(ErrorCode.INVALID_PASSWORD);
		}

		if (passwordEncoder.matches(request.newPassword(), member.getPassword())) {
			throw new CustomException(ErrorCode.SAME_AS_OLD_PASSWORD);
		}

		member.setPassword(passwordEncoder.encode(request.newPassword()));
	}
}
