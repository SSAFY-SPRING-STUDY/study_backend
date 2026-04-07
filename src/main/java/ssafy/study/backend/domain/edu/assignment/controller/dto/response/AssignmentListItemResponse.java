package ssafy.study.backend.domain.edu.assignment.controller.dto.response;

import ssafy.study.backend.domain.edu.assignment.entity.Assignment;
import ssafy.study.backend.domain.edu.assignment.entity.AssignmentProgressStatus;

public record AssignmentListItemResponse(
	Long assignmentId,
	String title,
	int orderInStudy,
	AssignmentProgressStatus myStatus  // 본인의 진행 상태 (없으면 null)
) {
	public static AssignmentListItemResponse from(Assignment assignment, AssignmentProgressStatus myStatus) {
		return new AssignmentListItemResponse(
			assignment.getId(),
			assignment.getTitle(),
			assignment.getOrderInStudy(),
			myStatus
		);
	}
}
