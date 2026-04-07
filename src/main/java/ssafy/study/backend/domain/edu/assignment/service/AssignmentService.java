package ssafy.study.backend.domain.edu.assignment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.assignment.controller.dto.request.AssignmentRequest;
import ssafy.study.backend.domain.edu.assignment.controller.dto.response.ApplyAssignmentResponse;
import ssafy.study.backend.domain.edu.assignment.controller.dto.response.AssignmentListItemResponse;
import ssafy.study.backend.domain.edu.assignment.controller.dto.response.AssignmentProgressResponse;
import ssafy.study.backend.domain.edu.assignment.controller.dto.response.AssignmentResponse;
import ssafy.study.backend.domain.edu.assignment.entity.Assignment;
import ssafy.study.backend.domain.edu.assignment.entity.AssignmentProgress;
import ssafy.study.backend.domain.edu.assignment.entity.AssignmentProgressStatus;
import ssafy.study.backend.domain.edu.assignment.repository.AssignmentProgressRepository;
import ssafy.study.backend.domain.edu.assignment.repository.AssignmentRepository;
import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.entity.StudyMember;
import ssafy.study.backend.domain.edu.study.repository.StudyRepository;
import ssafy.study.backend.domain.edu.study.service.StudyMemberService;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;
import ssafy.study.backend.global.github.GitHubClient;
import ssafy.study.backend.global.github.dto.GitHubIssueRequest;
import ssafy.study.backend.global.github.dto.GitHubIssueResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

	private final AssignmentRepository assignmentRepository;
	private final AssignmentProgressRepository assignmentProgressRepository;
	private final StudyRepository studyRepository;
	private final StudyMemberService studyMemberService;
	private final GitHubClient gitHubClient;

	/** 과제 생성 (LEADER 또는 ADMIN) */
	@Transactional
	public AssignmentResponse create(Long studyId, Long requesterId, AssignmentRequest request) {
		studyMemberService.validateLeaderOrAdmin(studyId, requesterId);

		Study study = findStudy(studyId);
		Assignment assignment = Assignment.builder()
			.study(study)
			.title(request.title())
			.content(request.content())
			.prTemplate(request.prTemplate())
			.orderInStudy(request.orderInStudy())
			.build();

		return AssignmentResponse.from(assignmentRepository.save(assignment));
	}

	/** 과제 목록 조회 (본인 진행 상태 포함) */
	public List<AssignmentListItemResponse> list(Long studyId, Long memberId) {
		StudyMember studyMember = studyMemberService.getStudyMemberOrThrow(studyId, memberId);
		List<Assignment> assignments = assignmentRepository.findByStudyIdOrderByOrderInStudyAsc(studyId);

		return assignments.stream()
			.map(assignment -> {
				AssignmentProgressStatus myStatus = assignmentProgressRepository
					.findByStudyMemberIdAndAssignmentId(studyMember.getId(), assignment.getId())
					.map(AssignmentProgress::getStatus)
					.orElse(null);
				return AssignmentListItemResponse.from(assignment, myStatus);
			})
			.toList();
	}

	/** 과제 상세 조회 */
	public AssignmentResponse get(Long assignmentId, Long memberId) {
		Assignment assignment = findAssignment(assignmentId);
		studyMemberService.getStudyMemberOrThrow(assignment.getStudy().getId(), memberId);
		return AssignmentResponse.from(assignment);
	}

	/** 과제 수정 (LEADER 또는 ADMIN) */
	@Transactional
	public AssignmentResponse update(Long assignmentId, Long requesterId, AssignmentRequest request) {
		Assignment assignment = findAssignment(assignmentId);
		studyMemberService.validateLeaderOrAdmin(assignment.getStudy().getId(), requesterId);
		assignment.update(request.title(), request.content(), request.prTemplate(), request.orderInStudy());
		return AssignmentResponse.from(assignment);
	}

	/** 과제 삭제 (LEADER 또는 ADMIN) */
	@Transactional
	public void delete(Long assignmentId, Long requesterId) {
		Assignment assignment = findAssignment(assignmentId);
		studyMemberService.validateLeaderOrAdmin(assignment.getStudy().getId(), requesterId);
		Long studyId = assignment.getStudy().getId();
		int deletedOrder = assignment.getOrderInStudy();
		assignmentRepository.delete(assignment);

		// 삭제된 순번 이후 과제들의 orderInStudy를 -1 재정렬 (같은 트랜잭션)
		assignmentRepository.findByStudyIdAndOrderInStudyGreaterThan(studyId, deletedOrder)
			.forEach(Assignment::decrementOrder);
	}

	/** 과제 신청 — GitHub Issue 생성 */
	@Transactional
	public ApplyAssignmentResponse apply(Long assignmentId, Long memberId) {
		Assignment assignment = findAssignment(assignmentId);
		Long studyId = assignment.getStudy().getId();

		StudyMember studyMember = studyMemberService.getStudyMemberOrThrow(studyId, memberId);

		if (assignmentProgressRepository.existsByStudyMemberIdAndAssignmentId(studyMember.getId(), assignmentId)) {
			throw new CustomException(ErrorCode.ASSIGNMENT_ALREADY_APPLIED);
		}

		// 이전 과제 완료 여부 검증 (첫 번째 과제는 예외)
		if (assignment.getOrderInStudy() > 1) {
			boolean prevIncomplete = assignmentProgressRepository
				.existsByStudyMemberIdAndAssignment_OrderInStudyLessThanAndStatusNot(
					studyMember.getId(), assignment.getOrderInStudy(), AssignmentProgressStatus.COMPLETED);
			if (prevIncomplete) {
				throw new CustomException(ErrorCode.ASSIGNMENT_PREREQUISITE_NOT_MET);
			}
		}

		if (!studyMember.getMember().isGithubLinked()) {
			throw new CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_LINKED);
		}

		Study study = assignment.getStudy();
		String issueBody = buildIssueBody(assignment);

		GitHubIssueRequest issueRequest = new GitHubIssueRequest(
			"[과제 " + assignment.getOrderInStudy() + "] " + assignment.getTitle(),
			issueBody,
			List.of(studyMember.getMember().getGithubUsername())
		);

		GitHubIssueResponse issueResponse = gitHubClient.createIssue(
			study.getGithubOrgName(), study.getGithubRepoName(), issueRequest);

		AssignmentProgress progress = AssignmentProgress.builder()
			.studyMember(studyMember)
			.assignment(assignment)
			.build();
		progress.setIssueNumber(issueResponse.number());
		assignmentProgressRepository.save(progress);

		// progress ID 확보 후 Issue body에 progress_id 삽입 (best-effort)
		String updatedBody = issueBody + "\n\n<!-- progress_id: " + progress.getId() + " -->";
		gitHubClient.updateIssueBody(
			study.getGithubOrgName(), study.getGithubRepoName(), issueResponse.number(), updatedBody);

		return ApplyAssignmentResponse.from(progress);
	}

	/** 내 과제별 상세 진행현황 */
	public List<AssignmentProgressResponse> getMyProgress(Long studyId, Long memberId) {
		StudyMember studyMember = studyMemberService.getStudyMemberOrThrow(studyId, memberId);
		List<AssignmentProgress> progressList =
			assignmentProgressRepository.findByStudyMemberId(studyMember.getId());
		return progressList.stream().map(AssignmentProgressResponse::from).toList();
	}

	/** Webhook에서 호출 — progress_id로 COMPLETED 처리 */
	@Transactional
	public void completeByProgressId(Long progressId) {
		AssignmentProgress progress = assignmentProgressRepository.findById(progressId)
			.orElseThrow(() -> new CustomException(ErrorCode.ASSIGNMENT_PROGRESS_NOT_FOUND));
		progress.complete();
	}

	// ===== private =====

	private Assignment findAssignment(Long assignmentId) {
		return assignmentRepository.findById(assignmentId)
			.orElseThrow(() -> new CustomException(ErrorCode.ASSIGNMENT_NOT_FOUND));
	}

	private Study findStudy(Long studyId) {
		return studyRepository.findById(studyId)
			.orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));
	}

	private String buildIssueBody(Assignment assignment) {
		// prTemplate의 {{PROGRESS_ID}}는 Issue 생성 전 progress ID를 모르므로
		// ID는 Issue 생성 후 별도 댓글이나 본문 업데이트로 처리하거나,
		// progress 저장 후 Issue body update API를 호출하는 방식으로 확장 가능.
		// 현재는 content + prTemplate을 그대로 포함하여 전송.
		return assignment.getContent()
			+ "\n\n---\n\n## PR 작성 가이드\n\n"
			+ assignment.getPrTemplate();
	}
}
