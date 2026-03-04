package ssafy.study.backend.domain.member.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
	@Size(max = 50, message = "이름은 50자 이하이어야 합니다.")
	@Schema(description = "수정할 이름", example = "새로운 이름")
	String name,

	@Size(min = 3, max = 50, message = "닉네임은 3자 이상 50자 이하이어야 합니다.")
	@Schema(description = "수정할 닉네임", example = "newNickname")
	String nickname
) {}
