package ssafy.study.backend.domain.member.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmProfileImageRequest(
	@NotBlank
	String imageKey
) {
}
