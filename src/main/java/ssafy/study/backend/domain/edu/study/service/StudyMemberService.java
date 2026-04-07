package ssafy.study.backend.domain.edu.study.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.study.controller.dto.response.StudyMemberListResponse;
import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.entity.StudyMember;
import ssafy.study.backend.domain.edu.study.entity.StudyMemberRole;
import ssafy.study.backend.domain.edu.study.repository.StudyMemberRepository;
import ssafy.study.backend.domain.edu.study.repository.StudyRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyMemberService {

	private final StudyRepository studyRepository;
	private final MemberRepository memberRepository;
	private final StudyMemberRepository studyMemberRepository;

	/** 스터디 참여 (GitHub 계정 연동 필수) */
	@Transactional
	public void join(Long studyId, Long memberId) {
		Study study = findStudy(studyId);
		Member member = findMember(memberId);

		if (!member.isGithubLinked()) {
			throw new CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_LINKED);
		}

		if (studyMemberRepository.existsByStudyIdAndMemberId(studyId, memberId)) {
			throw new CustomException(ErrorCode.STUDY_MEMBER_ALREADY_EXISTS);
		}

		StudyMember studyMember = StudyMember.builder()
			.study(study)
			.member(member)
			.role(StudyMemberRole.MEMBER)
			.build();
		studyMemberRepository.save(studyMember);
	}

	/** 스터디원 목록 + 진행현황 조회 */
	public StudyMemberListResponse getMembers(Long studyId) {
		findStudy(studyId);
		List<StudyMember> members = studyMemberRepository.findByStudyId(studyId);
		return StudyMemberListResponse.from(members);
	}

	/** 역할 변경 (LEADER 또는 ADMIN만 가능) */
	@Transactional
	public void changeRole(Long studyId, Long targetMemberId, StudyMemberRole newRole, Long requesterId) {
		validateLeaderOrAdmin(studyId, requesterId);

		StudyMember target = studyMemberRepository.findByStudyIdAndMemberId(studyId, targetMemberId)
			.orElseThrow(() -> new CustomException(ErrorCode.STUDY_MEMBER_NOT_FOUND));
		target.changeRole(newRole);
	}

	/** 스터디 탈퇴 */
	@Transactional
	public void leave(Long studyId, Long memberId) {
		StudyMember studyMember = studyMemberRepository.findByStudyIdAndMemberId(studyId, memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.STUDY_MEMBER_NOT_FOUND));

		if (studyMember.getRole() == StudyMemberRole.LEADER) {
			long leaderCount = studyMemberRepository.countByStudyIdAndRole(studyId, StudyMemberRole.LEADER);
			if (leaderCount <= 1) {
				long totalCount = studyMemberRepository.countByStudyId(studyId);
				if (totalCount > 1) {
					throw new CustomException(ErrorCode.LAST_LEADER_CANNOT_LEAVE);
				}
			}
		}

		studyMemberRepository.delete(studyMember);
	}

	// ===== helpers =====

	public StudyMember getStudyMemberOrThrow(Long studyId, Long memberId) {
		return studyMemberRepository.findByStudyIdAndMemberId(studyId, memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.STUDY_MEMBER_NOT_FOUND));
	}

	public void validateLeaderOrAdmin(Long studyId, Long memberId) {
		Member member = findMember(memberId);
		if (member.getRole() == ssafy.study.backend.domain.member.entity.MemberRole.ROLE_ADMIN) {
			return;
		}
		if (!studyMemberRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyMemberRole.LEADER)) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}
	}

	private Study findStudy(Long studyId) {
		return studyRepository.findById(studyId)
			.orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
	}
}
