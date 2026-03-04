package ssafy.study.backend.domain.member.controller.dto.response;

import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.entity.MemberLevel;
import ssafy.study.backend.domain.member.entity.MemberRole;

public record MemberInfo(
	Long id,
	String email,
	String name,
	String nickName,
	MemberLevel level,
	MemberRole role
) {
	public static MemberInfo fromEntity(Member member) {
		return new MemberInfo(
			member.getId(),
			member.getEmail(),
			member.getName(),
			member.getNickname(),
			member.getLevel(),
			member.getRole()
		);
	}
}
