package ssafy.study.backend.domain.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequestRequest(

	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Size(max = 100, message = "이메일은 100자 이하이어야 합니다.")
	@Schema(description = "비밀번호 재설정을 요청할 사용자의 email", example = "testUser@email.com")
	String email

) {
}
