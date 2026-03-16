package ssafy.study.backend.domain.edu.post.controller.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequest(

	@NotBlank(message = "제목은 필수입니다.")
	@Size(max = 100, message = "제목은 100자 이하이어야 합니다.")
	@Schema(description = "게시글 제목", example = "스프링 입문 1강")
	String title,

	@NotBlank(message = "내용은 필수입니다.")
	@Schema(description = "게시글 내용", example = "이번 강의에서는 스프링의 기본 개념을 학습합니다.")
	String content,

	@Schema(description = "게시글에 포함된 이미지 ID 목록 (없으면 생략 가능)")
	List<Long> imageIds
) {}
