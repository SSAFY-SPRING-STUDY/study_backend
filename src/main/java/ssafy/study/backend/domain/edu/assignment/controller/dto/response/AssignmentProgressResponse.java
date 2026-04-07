package ssafy.study.backend.domain.edu.assignment.controller.dto.response;

import java.time.LocalDateTime;

import ssafy.study.backend.domain.edu.assignment.entity.AssignmentProgress;
import ssafy.study.backend.domain.edu.assignment.entity.AssignmentProgressStatus;

public record AssignmentProgressResponse(
	Long progressId,
	Long assignmentId,
	String title,
	int orderInStudy,
	AssignmentProgressStatus status,
	Long githubIssueNumber,
	LocalDateTime appliedAt,
	LocalDateTime completedAt
) {
	public static AssignmentProgressResponse from(AssignmentProgress progress) {
		return new AssignmentProgressResponse(
			progress.getId(),
			progress.getAssignment().getId(),
			progress.getAssignment().getTitle(),
			progress.getAssignment().getOrderInStudy(),
			progress.getStatus(),
			progress.getGithubIssueNumber(),
			progress.getAppliedAt(),
			progress.getCompletedAt()
		);
	}
}
