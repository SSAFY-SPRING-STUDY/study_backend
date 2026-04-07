package ssafy.study.backend.domain.edu.assignment.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignmentRequest(
	@NotBlank(message = "과제 제목은 필수입니다.")
	String title,

	@NotBlank(message = "과제 내용은 필수입니다.")
	String content,

	@NotBlank(message = "PR 템플릿은 필수입니다.")
	String prTemplate,

	@NotNull(message = "과제 순서는 필수입니다.")
	@Min(value = 1, message = "과제 순서는 1 이상이어야 합니다.")
	Integer orderInStudy
) {
}
