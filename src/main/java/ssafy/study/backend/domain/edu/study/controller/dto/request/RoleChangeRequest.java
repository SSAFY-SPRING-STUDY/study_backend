package ssafy.study.backend.domain.edu.study.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import ssafy.study.backend.domain.edu.study.entity.StudyMemberRole;

public record RoleChangeRequest(
	@NotNull(message = "역할은 필수입니다.")
	StudyMemberRole role
) {
}
