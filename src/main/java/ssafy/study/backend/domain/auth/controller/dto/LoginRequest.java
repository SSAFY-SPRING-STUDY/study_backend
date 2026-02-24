package ssafy.study.backend.domain.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Schema(description = "로그인할 사용자의 email", example = "testUser@email.com")
	String email,

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Schema(description = "로그인할 사용자의 password", example = "password123")
	String password

) {
}
