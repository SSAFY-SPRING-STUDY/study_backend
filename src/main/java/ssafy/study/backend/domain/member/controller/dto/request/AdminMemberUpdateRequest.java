package ssafy.study.backend.domain.member.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import ssafy.study.backend.domain.member.entity.MemberLevel;
import ssafy.study.backend.domain.member.entity.MemberRole;

public record AdminMemberUpdateRequest(
	@Size(max = 50, message = "이름은 50자 이하이어야 합니다.")
	@Schema(description = "변경할 이름 (null이면 수정 안 함)", example = "홍길동")
	String name,

	@Size(min = 1, max = 50, message = "닉네임은 1자 이상 50자 이하이어야 합니다.")
	@Schema(description = "변경할 닉네임 (null이면 수정 안 함)", example = "gildong")
	String nickname,

	@Schema(description = "변경할 역할 (null이면 수정 안 함)", example = "ROLE_ADMIN")
	MemberRole role,

	@Schema(description = "변경할 레벨 (null이면 수정 안 함)", example = "INTERMEDIATE")
	MemberLevel level
) {}
