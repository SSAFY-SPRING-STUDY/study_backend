package ssafy.study.backend.domain.study.post.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(

	@NotBlank(message = "제목은 필수입니다.")
	@Size(max = 100, message = "제목은 100자 이하이어야 합니다.")
	@Schema(description = "게시글 제목", example = "스프링 입문 1강")
	String title,

	@NotBlank(message = "내용은 필수입니다.")
	@Schema(description = "게시글 내용", example = "이번 강의에서는 스프링의 기본 개념을 학습합니다.")
	String content
) {}
