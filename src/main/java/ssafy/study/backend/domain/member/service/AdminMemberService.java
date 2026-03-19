package ssafy.study.backend.domain.member.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.member.controller.dto.request.AdminMemberUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.response.MemberInfo;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

	private final MemberRepository memberRepository;

	public Page<MemberInfo> getMembers(String keyword, Pageable pageable) {
		String trimmed = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
		return memberRepository.searchByKeyword(trimmed, pageable).map(MemberInfo::fromEntity);
	}

	public MemberInfo getMember(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
		return MemberInfo.fromEntity(member);
	}

	@Transactional
	public MemberInfo updateMember(Long memberId, AdminMemberUpdateRequest request) {
		Member member = memberRepository.findById(memberId)
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

		if (request.role() != null) {
			member.updateRole(request.role());
		}

		if (request.level() != null) {
			member.updateLevel(request.level());
		}

		return MemberInfo.fromEntity(member);
	}
}
