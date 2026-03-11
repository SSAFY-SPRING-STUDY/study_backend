package ssafy.study.backend.domain.edu.image.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ImageRequest(
	@NotBlank(message = "컨텐츠 타입은 필수입니다.")
	String contentType,  // image/png 등

	@NotNull(message = "파일 크기는 필수입니다.")
	@Positive(message = "파일 크기는 0보다 커야 합니다.")
	Long contentLength,

	@NotBlank(message = "파일명은 필수입니다.")
	String fileName
) {}
