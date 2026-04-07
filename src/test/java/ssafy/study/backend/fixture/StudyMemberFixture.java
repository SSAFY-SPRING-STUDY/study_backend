package ssafy.study.backend.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.entity.StudyMember;
import ssafy.study.backend.domain.edu.study.entity.StudyMemberRole;
import ssafy.study.backend.domain.member.entity.Member;

public class StudyMemberFixture {

	public static StudyMember leader(Long id, Study study, Member member) {
		StudyMember sm = StudyMember.builder()
			.study(study)
			.member(member)
			.role(StudyMemberRole.LEADER)
			.build();
		ReflectionTestUtils.setField(sm, "id", id);
		return sm;
	}

	public static StudyMember regularMember(Long id, Study study, Member member) {
		StudyMember sm = StudyMember.builder()
			.study(study)
			.member(member)
			.role(StudyMemberRole.MEMBER)
			.build();
		ReflectionTestUtils.setField(sm, "id", id);
		return sm;
	}
}
