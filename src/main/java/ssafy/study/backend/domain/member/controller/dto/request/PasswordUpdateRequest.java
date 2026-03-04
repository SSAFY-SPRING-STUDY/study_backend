package ssafy.study.backend.domain.member.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(
	@Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하이어야 합니다.")
	@Schema(description = "수정할 비밀번호 (수정 안 할 경우 제외)", example = "newPassword123")
	String newPassword
) {}
