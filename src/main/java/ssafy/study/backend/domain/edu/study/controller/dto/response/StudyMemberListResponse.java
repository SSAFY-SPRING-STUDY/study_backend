package ssafy.study.backend.domain.edu.study.controller.dto.response;

import java.util.List;

import ssafy.study.backend.domain.edu.study.entity.StudyMember;

public record StudyMemberListResponse(
	List<StudyMemberInfo> members
) {
	public static StudyMemberListResponse from(List<StudyMember> studyMembers) {
		return new StudyMemberListResponse(
			studyMembers.stream().map(StudyMemberInfo::from).toList()
		);
	}
}
