package ssafy.study.backend.domain.edu.study.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.study.controller.dto.response.StudyMemberListResponse;
import ssafy.study.backend.global.aws.s3.S3Service;
import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.entity.StudyMember;
import ssafy.study.backend.domain.edu.study.entity.StudyMemberRole;
import ssafy.study.backend.domain.edu.study.repository.StudyMemberRepository;
import ssafy.study.backend.domain.edu.study.repository.StudyRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.entity.MemberRole;
import ssafy.study.backend.domain.member.repository.MemberProfileRepository;
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
	private final MemberProfileRepository memberProfileRepository;
	private final S3Service s3Service;

	/** 스터디 참여 */
	@Transactional
	public void join(Long studyId, Long memberId) {
		Study study = findStudy(studyId);
		Member member = findMember(memberId);

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

		List<Long> memberIds = members.stream().map(sm -> sm.getMember().getId()).toList();
		Map<Long, String> profileImageUrls = memberProfileRepository.findAllByMemberIdIn(memberIds).stream()
			.filter(p -> p.getProfileImageKey() != null)
			.collect(Collectors.toMap(
				p -> p.getMember().getId(),
				p -> s3Service.getDownloadPresignedUrl(p.getProfileImageKey())
			));

		return StudyMemberListResponse.from(members, profileImageUrls);
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

	/** 스터디 생성 시 생성자를 LEADER로 등록 */
	@Transactional
	public void registerLeader(Study study, Long memberId) {
		Member member = findMember(memberId);
		StudyMember studyMember = StudyMember.builder()
			.study(study)
			.member(member)
			.role(StudyMemberRole.LEADER)
			.build();
		studyMemberRepository.save(studyMember);
	}

	/** 인증 여부·스터디 멤버 여부 무관하게 Optional 반환 (조회 공개 허용용) */
	public Optional<StudyMember> findStudyMemberOptional(Long studyId, Long memberId) {
		if (memberId == null) {
			return Optional.empty();
		}
		return studyMemberRepository.findByStudyIdAndMemberId(studyId, memberId);
	}

	/** ADMIN이면 Optional 반환(우회), 일반 유저는 없을 시 404 */
	public Optional<StudyMember> findStudyMemberIfNotAdmin(Long studyId, Long memberId) {
		Member member = findMember(memberId);
		if (member.getRole() == MemberRole.ROLE_ADMIN) {
			return studyMemberRepository.findByStudyIdAndMemberId(studyId, memberId);
		}
		return Optional.of(getStudyMemberOrThrow(studyId, memberId));
	}

	public StudyMember getStudyMemberOrThrow(Long studyId, Long memberId) {
		return studyMemberRepository.findByStudyIdAndMemberId(studyId, memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.STUDY_MEMBER_NOT_FOUND));
	}

	public void validateLeaderOrAdmin(Long studyId, Long memberId) {
		Member member = findMember(memberId);
		if (member.getRole() == MemberRole.ROLE_ADMIN) {
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