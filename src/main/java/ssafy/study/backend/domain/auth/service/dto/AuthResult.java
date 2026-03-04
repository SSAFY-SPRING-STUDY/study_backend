package ssafy.study.backend.domain.auth.service.dto;

import ssafy.study.backend.domain.member.controller.dto.response.MemberInfo;

public record AuthResult(
	MemberInfo memberInfo,
	String accessToken,
	String refreshToken
) {
}
