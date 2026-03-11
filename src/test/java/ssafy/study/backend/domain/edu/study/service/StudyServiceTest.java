package ssafy.study.backend.domain.edu.study.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import ssafy.study.backend.domain.edu.study.controller.dto.request.StudyRequest;
import ssafy.study.backend.domain.edu.study.controller.dto.request.StudyResponse;
import ssafy.study.backend.domain.edu.study.entity.DifficultyLevel;
import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.entity.StudyType;
import ssafy.study.backend.domain.edu.study.repository.StudyRepository;
import ssafy.study.backend.fixture.StudyFixture;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

	@InjectMocks
	private StudyService studyService;

	@Mock
	private StudyRepository studyRepository;

	@Test
	@DisplayName("스터디 생성 성공")
	void 스터디_생성_성공() {
		// given
		StudyRequest request = new StudyRequest("스프링 백엔드", "스프링 학습 스터디", DifficultyLevel.BASIC, StudyType.BACKEND);
		Study study = StudyFixture.study(1L);
		given(studyRepository.save(any(Study.class))).willReturn(study);

		// when
		StudyResponse result = studyService.createStudy(request);

		// then
		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.name()).isEqualTo(study.getName());
	}

	@Test
	@DisplayName("스터디 수정 성공")
	void 스터디_수정_성공() {
		// given
		Study study = StudyFixture.study(1L);
		StudyRequest request = new StudyRequest("수정된 이름", "수정된 설명", DifficultyLevel.INTERMEDIATE, StudyType.ALGORITHM);
		given(studyRepository.findById(1L)).willReturn(Optional.of(study));

		// when
		StudyResponse result = studyService.updateStudy(1L, request);

		// then
		assertThat(result.name()).isEqualTo("수정된 이름");
		assertThat(result.description()).isEqualTo("수정된 설명");
		assertThat(result.level()).isEqualTo(DifficultyLevel.INTERMEDIATE.name());
		assertThat(result.type()).isEqualTo(StudyType.ALGORITHM.name());
	}

	@Test
	@DisplayName("스터디 수정 실패 - 존재하지 않는 스터디")
	void 스터디_수정_실패_존재하지_않는_스터디() {
		// given
		StudyRequest request = new StudyRequest("수정된 이름", "수정된 설명", DifficultyLevel.BASIC, StudyType.BACKEND);
		given(studyRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> studyService.updateStudy(999L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_NOT_FOUND);
	}

	@Test
	@DisplayName("스터디 삭제 성공")
	void 스터디_삭제_성공() {
		// given
		willDoNothing().given(studyRepository).deleteById(1L);

		// when & then
		assertThatNoException().isThrownBy(() -> studyService.deleteStudy(1L));
		then(studyRepository).should().deleteById(1L);
	}

	@Test
	@DisplayName("스터디 단건 조회 성공")
	void 스터디_단건_조회_성공() {
		// given
		Study study = StudyFixture.study(1L);
		given(studyRepository.findById(1L)).willReturn(Optional.of(study));

		// when
		StudyResponse result = studyService.getStudy(1L);

		// then
		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.name()).isEqualTo(study.getName());
	}

	@Test
	@DisplayName("스터디 단건 조회 실패 - 존재하지 않는 스터디")
	void 스터디_단건_조회_실패_존재하지_않는_스터디() {
		// given
		given(studyRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> studyService.getStudy(999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_NOT_FOUND);
	}

	@Test
	@DisplayName("타입별 스터디 목록 조회 성공")
	void 타입별_스터디_목록_조회_성공() {
		// given
		Study study1 = StudyFixture.study(1L);
		Study study2 = StudyFixture.studyBuilder().name("다른 스터디").build();
		PageRequest pageable = PageRequest.of(0, 10);
		Page<Study> studyPage = new PageImpl<>(List.of(study1, study2), pageable, 2);
		given(studyRepository.findByType(StudyType.BACKEND, pageable)).willReturn(studyPage);

		// when
		Page<StudyResponse> result = studyService.getStudiesByType(StudyType.BACKEND, pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getContent()).hasSize(2);
	}
}
