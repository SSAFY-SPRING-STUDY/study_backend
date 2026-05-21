package ssafy.study.backend.domain.member.controller.dto.response;

import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.entity.MemberLevel;
import ssafy.study.backend.domain.member.entity.MemberProfile;
import ssafy.study.backend.domain.member.entity.MemberRole;

public record MemberDetailInfo(
	Long id,
	String email,
	String name,
	String nickname,
	MemberLevel level,
	MemberRole role,
	String githubUsername,
	String description,
	String profileImageUrl
) {
	public static MemberDetailInfo fromEntity(Member member, MemberProfile profile, String profileImageUrl) {
		return new MemberDetailInfo(
			member.getId(),
			member.getEmail(),
			member.getName(),
			member.getNickname(),
			member.getLevel(),
			member.getRole(),
			member.getGithubUsername(),
			profile.getDescription(),
			profileImageUrl
		);
	}
}
