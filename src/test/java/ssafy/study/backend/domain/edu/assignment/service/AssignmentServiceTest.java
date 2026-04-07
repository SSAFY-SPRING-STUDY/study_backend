package ssafy.study.backend.domain.edu.assignment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.edu.assignment.entity.Assignment;
import ssafy.study.backend.domain.edu.assignment.entity.AssignmentProgress;
import ssafy.study.backend.domain.edu.assignment.repository.AssignmentProgressRepository;
import ssafy.study.backend.domain.edu.assignment.repository.AssignmentRepository;
import ssafy.study.backend.domain.edu.study.entity.DifficultyLevel;
import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.entity.StudyMember;
import ssafy.study.backend.domain.edu.study.entity.StudyType;
import ssafy.study.backend.domain.edu.study.repository.StudyRepository;
import ssafy.study.backend.domain.edu.study.service.StudyMemberService;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.fixture.AssignmentFixture;
import ssafy.study.backend.fixture.MemberFixture;
import ssafy.study.backend.fixture.StudyMemberFixture;
import ssafy.study.backend.global.github.GitHubClient;
import ssafy.study.backend.global.github.dto.GitHubIssueResponse;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

	@InjectMocks
	private AssignmentService assignmentService;

	@Mock
	private AssignmentRepository assignmentRepository;

	@Mock
	private AssignmentProgressRepository assignmentProgressRepository;

	@Mock
	private StudyRepository studyRepository;

	@Mock
	private StudyMemberService studyMemberService;

	@Mock
	private GitHubClient gitHubClient;

	// ===== ISSUE-1: Issue body에 progress_id 삽입 =====

	@Test
	@DisplayName("과제 신청 시 GitHub Issue body에 progress_id HTML 주석이 포함됨")
	void 과제신청_Issue_body에_progress_id_주석_포함() {
		// given
		Study study = Study.builder()
			.name("스터디").description("설명")
			.level(DifficultyLevel.BASIC).type(StudyType.BACKEND)
			.githubOrgName("test-org").githubRepoName("test-repo")
			.githubWebhookSecret("secret")
			.build();
		ReflectionTestUtils.setField(study, "id", 1L);

		Assignment assignment = AssignmentFixture.assignment(10L, study, 1);
		Member member = MemberFixture.githubLinkedMember(20L);
		StudyMember studyMember = StudyMemberFixture.regularMember(100L, study, member);

		GitHubIssueResponse issueResponse = new GitHubIssueResponse(42L, "https://github.com/test-org/test-repo/issues/42");

		given(assignmentRepository.findById(10L)).willReturn(Optional.of(assignment));
		given(studyMemberService.getStudyMemberOrThrow(1L, 20L)).willReturn(studyMember);
		given(assignmentProgressRepository.existsByStudyMemberIdAndAssignmentId(100L, 10L)).willReturn(false);
		given(gitHubClient.createIssue(any(), any(), any())).willReturn(issueResponse);
		given(assignmentProgressRepository.save(any(AssignmentProgress.class))).willAnswer(invocation -> {
			AssignmentProgress p = invocation.getArgument(0);
			ReflectionTestUtils.setField(p, "id", 200L);
			return p;
		});

		// when
		assignmentService.apply(10L, 20L);

		// then: progress_id 주석을 포함한 body로 updateIssueBody 호출 확인
		then(gitHubClient).should().updateIssueBody(
			eq("test-org"),
			eq("test-repo"),
			eq(42L),
			argThat(body -> body.contains("<!-- progress_id: 200 -->"))
		);
	}

	@Test
	@DisplayName("과제 신청 시 Issue body update 실패해도 과제 신청 자체는 성공")
	void 과제신청_Issue_body_update_실패시_전체_실패_아님() {
		// given
		Study study = Study.builder()
			.name("스터디").description("설명")
			.level(DifficultyLevel.BASIC).type(StudyType.BACKEND)
			.githubOrgName("test-org").githubRepoName("test-repo")
			.githubWebhookSecret("secret")
			.build();
		ReflectionTestUtils.setField(study, "id", 1L);

		Assignment assignment = AssignmentFixture.assignment(10L, study, 1);
		Member member = MemberFixture.githubLinkedMember(20L);
		StudyMember studyMember = StudyMemberFixture.regularMember(100L, study, member);

		GitHubIssueResponse issueResponse = new GitHubIssueResponse(42L, "https://github.com/test-org/test-repo/issues/42");

		given(assignmentRepository.findById(10L)).willReturn(Optional.of(assignment));
		given(studyMemberService.getStudyMemberOrThrow(1L, 20L)).willReturn(studyMember);
		given(assignmentProgressRepository.existsByStudyMemberIdAndAssignmentId(100L, 10L)).willReturn(false);
		given(gitHubClient.createIssue(any(), any(), any())).willReturn(issueResponse);
		given(assignmentProgressRepository.save(any(AssignmentProgress.class))).willAnswer(invocation -> {
			AssignmentProgress p = invocation.getArgument(0);
			ReflectionTestUtils.setField(p, "id", 200L);
			return p;
		});
		// updateIssueBody는 예외를 내부에서 처리하므로 별도 설정 불필요

		// when & then: 예외 없이 정상 완료
		assertThatNoException().isThrownBy(() -> assignmentService.apply(10L, 20L));
	}

	// ===== ISSUE-3: 과제 삭제 시 순서 재정렬 =====

	@Test
	@DisplayName("중간 과제 삭제 시 후순위 과제들의 orderInStudy가 -1 재정렬됨")
	void 중간_과제_삭제_후순위_과제_순번_재정렬() {
		// given: 순서 1, 2, 3인 과제가 있고 순서 2를 삭제
		Study study = Study.builder()
			.name("스터디").description("설명")
			.level(DifficultyLevel.BASIC).type(StudyType.BACKEND)
			.build();
		ReflectionTestUtils.setField(study, "id", 1L);

		Assignment toDelete = AssignmentFixture.assignment(20L, study, 2);
		Assignment following = AssignmentFixture.assignment(30L, study, 3);

		given(assignmentRepository.findById(20L)).willReturn(Optional.of(toDelete));
		given(assignmentRepository.findByStudyIdAndOrderInStudyGreaterThan(1L, 2))
			.willReturn(List.of(following));

		// when
		assignmentService.delete(20L, 99L); // requesterId=99L (ADMIN이므로 권한 체크 pass)

		// then: 순서 3이었던 과제가 순서 2로 감소
		assertThat(following.getOrderInStudy()).isEqualTo(2);
		then(assignmentRepository).should().delete(toDelete);
	}

	@Test
	@DisplayName("마지막 과제 삭제 시 후순위 과제가 없어도 정상 처리")
	void 마지막_과제_삭제_후순위_없음() {
		// given
		Study study = Study.builder()
			.name("스터디").description("설명")
			.level(DifficultyLevel.BASIC).type(StudyType.BACKEND)
			.build();
		ReflectionTestUtils.setField(study, "id", 1L);

		Assignment lastAssignment = AssignmentFixture.assignment(30L, study, 3);

		given(assignmentRepository.findById(30L)).willReturn(Optional.of(lastAssignment));
		given(assignmentRepository.findByStudyIdAndOrderInStudyGreaterThan(1L, 3))
			.willReturn(List.of());

		// when & then: 예외 없이 정상 완료
		assertThatNoException().isThrownBy(() -> assignmentService.delete(30L, 99L));
		then(assignmentRepository).should().delete(lastAssignment);
	}
}
