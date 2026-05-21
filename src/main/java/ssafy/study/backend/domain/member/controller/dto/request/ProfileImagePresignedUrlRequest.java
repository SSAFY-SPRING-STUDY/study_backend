package ssafy.study.backend.domain.member.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProfileImagePresignedUrlRequest(
	@NotBlank
	String contentType,
	@Positive
	Long contentLength
) {
}
