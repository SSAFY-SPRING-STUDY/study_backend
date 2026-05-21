package ssafy.study.backend.domain.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(

	@NotBlank(message = "토큰은 필수입니다.")
	@Schema(description = "이메일로 발송된 재설정 토큰", example = "550e8400-e29b-41d4-a716-446655440000")
	String token,

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하이어야 합니다.")
	@Schema(description = "새 비밀번호", example = "newPassword123")
	String newPassword

) {
}
