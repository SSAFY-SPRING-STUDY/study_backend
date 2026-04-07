package ssafy.study.backend.domain.edu.study.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.edu.study.entity.DifficultyLevel;
import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.entity.StudyMember;
import ssafy.study.backend.domain.edu.study.entity.StudyMemberRole;
import ssafy.study.backend.domain.edu.study.entity.StudyType;
import ssafy.study.backend.domain.edu.study.repository.StudyMemberRepository;
import ssafy.study.backend.domain.edu.study.repository.StudyRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.fixture.MemberFixture;
import ssafy.study.backend.fixture.StudyMemberFixture;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class StudyMemberServiceTest {

	@InjectMocks
	private StudyMemberService studyMemberService;

	@Mock
	private StudyRepository studyRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private StudyMemberRepository studyMemberRepository;

	// ===== ISSUE-5: 유일 LEADER 탈퇴 방지 =====

	@Test
	@DisplayName("유일 LEADER가 탈퇴 시도 시 LAST_LEADER_CANNOT_LEAVE 에러 반환")
	void 유일_LEADER_탈퇴시도_에러() {
		// given
		Study study = makeStudy(1L);
		Member member = MemberFixture.member(10L);
		StudyMember leaderMember = StudyMemberFixture.leader(100L, study, member);

		given(studyMemberRepository.findByStudyIdAndMemberId(1L, 10L))
			.willReturn(Optional.of(leaderMember));
		given(studyMemberRepository.countByStudyIdAndRole(1L, StudyMemberRole.LEADER))
			.willReturn(1L); // 리더가 본인 1명뿐
		given(studyMemberRepository.countByStudyId(1L))
			.willReturn(3L); // 다른 멤버들도 있음

		// when & then
		assertThatThrownBy(() -> studyMemberService.leave(1L, 10L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.LAST_LEADER_CANNOT_LEAVE);
	}

	@Test
	@DisplayName("일반 MEMBER는 리더 체크 없이 탈퇴 성공")
	void 일반_MEMBER_탈퇴_성공() {
		// given
		Study study = makeStudy(1L);
		Member member = MemberFixture.member(10L);
		StudyMember regularMember = StudyMemberFixture.regularMember(100L, study, member);

		given(studyMemberRepository.findByStudyIdAndMemberId(1L, 10L))
			.willReturn(Optional.of(regularMember));

		// when & then
		assertThatNoException().isThrownBy(() -> studyMemberService.leave(1L, 10L));
		then(studyMemberRepository).should().delete(regularMember);
		// LEADER 카운트 조회가 호출되지 않아야 함
		then(studyMemberRepository).should(never()).countByStudyIdAndRole(any(), any());
	}

	@Test
	@DisplayName("LEADER가 2명일 때 한 명이 탈퇴 → 성공")
	void LEADER_2명_중_1명_탈퇴_성공() {
		// given
		Study study = makeStudy(1L);
		Member member = MemberFixture.member(10L);
		StudyMember leaderMember = StudyMemberFixture.leader(100L, study, member);

		given(studyMemberRepository.findByStudyIdAndMemberId(1L, 10L))
			.willReturn(Optional.of(leaderMember));
		given(studyMemberRepository.countByStudyIdAndRole(1L, StudyMemberRole.LEADER))
			.willReturn(2L); // 리더가 2명

		// when & then
		assertThatNoException().isThrownBy(() -> studyMemberService.leave(1L, 10L));
		then(studyMemberRepository).should().delete(leaderMember);
	}

	@Test
	@DisplayName("스터디 멤버가 본인 1명만 있을 때 LEADER 탈퇴 허용")
	void 멤버_1명뿐인_LEADER_탈퇴_허용() {
		// given
		Study study = makeStudy(1L);
		Member member = MemberFixture.member(10L);
		StudyMember leaderMember = StudyMemberFixture.leader(100L, study, member);

		given(studyMemberRepository.findByStudyIdAndMemberId(1L, 10L))
			.willReturn(Optional.of(leaderMember));
		given(studyMemberRepository.countByStudyIdAndRole(1L, StudyMemberRole.LEADER))
			.willReturn(1L); // 리더가 본인 1명
		given(studyMemberRepository.countByStudyId(1L))
			.willReturn(1L); // 전체 멤버도 1명 (본인만)

		// when & then: 예외 없이 탈퇴 성공
		assertThatNoException().isThrownBy(() -> studyMemberService.leave(1L, 10L));
		then(studyMemberRepository).should().delete(leaderMember);
	}

	private Study makeStudy(Long id) {
		Study study = Study.builder()
			.name("스터디").description("설명")
			.level(DifficultyLevel.BASIC).type(StudyType.BACKEND)
			.build();
		ReflectionTestUtils.setField(study, "id", id);
		return study;
	}
}
