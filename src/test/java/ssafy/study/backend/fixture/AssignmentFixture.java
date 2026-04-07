package ssafy.study.backend.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.edu.assignment.entity.Assignment;
import ssafy.study.backend.domain.edu.assignment.entity.AssignmentProgress;
import ssafy.study.backend.domain.edu.study.entity.StudyMember;

public class AssignmentFixture {

	public static Assignment assignment(Long id, ssafy.study.backend.domain.edu.study.entity.Study study, int order) {
		Assignment a = Assignment.builder()
			.study(study)
			.title("테스트 과제 " + order)
			.content("과제 내용")
			.prTemplate("PR 템플릿")
			.orderInStudy(order)
			.build();
		ReflectionTestUtils.setField(a, "id", id);
		return a;
	}

	public static AssignmentProgress appliedProgress(Long id, StudyMember studyMember, Assignment assignment) {
		AssignmentProgress p = AssignmentProgress.builder()
			.studyMember(studyMember)
			.assignment(assignment)
			.build();
		p.setIssueNumber(99L);
		ReflectionTestUtils.setField(p, "id", id);
		return p;
	}
}
