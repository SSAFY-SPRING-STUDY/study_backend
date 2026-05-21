package ssafy.study.backend.domain.edu.study.controller.dto.response;

import java.time.LocalDateTime;

import ssafy.study.backend.domain.edu.study.entity.StudyMember;
import ssafy.study.backend.domain.edu.study.entity.StudyMemberRole;

public record StudyMemberInfo(
	Long memberId,
	String nickname,
	String name,
	String githubUsername,
	StudyMemberRole role,
	LocalDateTime joinedAt,
	String profileImageUrl
) {
	public static StudyMemberInfo from(StudyMember sm, String profileImageUrl) {
		return new StudyMemberInfo(
			sm.getMember().getId(),
			sm.getMember().getNickname(),
			sm.getMember().getName(),
			sm.getMember().getGithubUsername(),
			sm.getRole(),
			sm.getJoinedAt(),
			profileImageUrl
		);
	}
}