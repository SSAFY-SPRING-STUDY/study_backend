package ssafy.study.backend.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.edu.curriculum.entity.Curriculum;
import ssafy.study.backend.domain.edu.study.entity.Study;

public class CurriculumFixture {

	public static Curriculum curriculum(Study study) {
		return curriculumBuilder(study).build();
	}

	public static Curriculum curriculum(Long id, Study study) {
		Curriculum curriculum = curriculumBuilder(study).build();
		ReflectionTestUtils.setField(curriculum, "id", id);
		return curriculum;
	}

	public static Curriculum.CurriculumBuilder curriculumBuilder(Study study) {
		return Curriculum.builder()
			.name("테스트 커리큘럼")
			.description("테스트 커리큘럼 설명")
			.orderInStudy(1)
			.study(study);
	}
}