package ssafy.study.backend.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.edu.study.entity.DifficultyLevel;
import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.entity.StudyType;

public class StudyFixture {

	public static Study study() {
		return studyBuilder().build();
	}

	public static Study study(Long id) {
		Study study = studyBuilder().build();
		ReflectionTestUtils.setField(study, "id", id);
		return study;
	}

	public static Study.StudyBuilder studyBuilder() {
		return Study.builder()
			.name("테스트 스터디")
			.description("테스트 스터디 설명")
			.level(DifficultyLevel.BASIC)
			.type(StudyType.BACKEND);
	}
}
