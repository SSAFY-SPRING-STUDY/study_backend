package ssafy.study.backend.domain.member.controller.dto.response;

import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.entity.MemberLevel;
import ssafy.study.backend.domain.member.entity.MemberRole;

public record MemberDetailInfo(
	Long id,
	String email,
	String name,
	String nickName,
	MemberLevel level,
	MemberRole role
) {
	public static MemberDetailInfo fromEntity(Member member) {
		return new MemberDetailInfo(
			member.getId(),
			member.getEmail(),
			member.getName(),
			member.getNickname(),
			member.getLevel(),
			member.getRole()
		);
	}
}
