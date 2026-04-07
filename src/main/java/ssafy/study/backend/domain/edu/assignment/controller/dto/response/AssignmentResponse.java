package ssafy.study.backend.domain.edu.assignment.controller.dto.response;

import java.time.LocalDateTime;

import ssafy.study.backend.domain.edu.assignment.entity.Assignment;

public record AssignmentResponse(
	Long assignmentId,
	Long studyId,
	String title,
	String content,
	String prTemplate,
	int orderInStudy,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static AssignmentResponse from(Assignment assignment) {
		return new AssignmentResponse(
			assignment.getId(),
			assignment.getStudy().getId(),
			assignment.getTitle(),
			assignment.getContent(),
			assignment.getPrTemplate(),
			assignment.getOrderInStudy(),
			assignment.getCreatedAt(),
			assignment.getUpdatedAt()
		);
	}
}
