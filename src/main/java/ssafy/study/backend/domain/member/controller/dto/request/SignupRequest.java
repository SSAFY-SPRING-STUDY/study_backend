package ssafy.study.backend.domain.member.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Size(max = 100, message = "이메일은 100자 이하이어야 합니다.")
	@Schema(description = "회원가입할 사용자의 email", example = "testUser@email.com")
	String email,

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하이어야 합니다.")
	@Schema(description = "회원가입할 사용자의 password", example = "password123")
	String password,

	@NotBlank(message = "이름은 필수입니다.")
	@Size(max = 50, message = "이름은 50자 이하이어야 합니다.")
	@Schema(description = "회원가입할 사용자의 이름", example = "테스트 유저")
	String name,

	@NotBlank(message = "닉네임은 필수입니다.")
	@Size(min = 3, max = 50, message = "닉네임은 3자 이상 50자 이하이어야 합니다.")
	@Schema(description = "회원가입할 사용자의 닉네임", example = "testUser")
	String nickname
) {
}