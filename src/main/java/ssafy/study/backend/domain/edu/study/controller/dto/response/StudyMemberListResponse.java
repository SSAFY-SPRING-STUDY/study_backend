package ssafy.study.backend.domain.edu.study.controller.dto.response;

import java.util.List;
import java.util.Map;

import ssafy.study.backend.domain.edu.study.entity.StudyMember;

public record StudyMemberListResponse(
	List<StudyMemberInfo> members
) {
	public static StudyMemberListResponse from(List<StudyMember> studyMembers, Map<Long, String> profileImageUrls) {
		return new StudyMemberListResponse(
			studyMembers.stream()
				.map(sm -> StudyMemberInfo.from(sm, profileImageUrls.get(sm.getMember().getId())))
				.toList()
		);
	}
}
