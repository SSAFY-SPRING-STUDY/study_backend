package ssafy.study.backend.domain.edu.study.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ssafy.study.backend.domain.edu.study.entity.DifficultyLevel;
import ssafy.study.backend.domain.edu.study.entity.StudyType;

public record StudyCreateRequest(

	@NotBlank(message = "스터디 이름은 필수입니다.")
	@Size(max = 100, message = "스터디 이름은 100자 이하이어야 합니다.")
	@Schema(description = "스터디 이름", example = "백엔드 스터디 1기")
	String name,

	@Size(max = 500, message = "스터디 설명은 500자 이하이어야 합니다.")
	@Schema(description = "스터디 설명", example = "스프링 심화 과정")
	String description,

	@NotNull(message = "스터디 레벨은 필수입니다.")
	@Schema(description = "스터디 난이도", example = "BASIC")
	DifficultyLevel level,

	@NotNull(message = "스터디 타입은 필수입니다.")
	@Schema(description = "스터디 타입", example = "BACKEND")
	StudyType type

) {}
