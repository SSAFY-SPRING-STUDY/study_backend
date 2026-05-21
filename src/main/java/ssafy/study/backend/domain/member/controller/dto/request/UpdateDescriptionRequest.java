package ssafy.study.backend.domain.member.controller.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateDescriptionRequest(
	@Size(max = 500)
	String description
) {
}
