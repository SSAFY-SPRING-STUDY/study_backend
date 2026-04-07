package ssafy.study.backend.domain.edu.assignment.controller.dto.response;

import ssafy.study.backend.domain.edu.assignment.entity.AssignmentProgress;
import ssafy.study.backend.domain.edu.assignment.entity.AssignmentProgressStatus;

public record ApplyAssignmentResponse(
	Long progressId,
	Long githubIssueNumber,
	AssignmentProgressStatus status
) {
	public static ApplyAssignmentResponse from(AssignmentProgress progress) {
		return new ApplyAssignmentResponse(
			progress.getId(),
			progress.getGithubIssueNumber(),
			progress.getStatus()
		);
	}
}
