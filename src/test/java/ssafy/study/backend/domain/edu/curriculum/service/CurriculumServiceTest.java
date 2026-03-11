package ssafy.study.backend.domain.edu.curriculum.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import ssafy.study.backend.domain.edu.curriculum.controller.dto.CurriculumRequest;
import ssafy.study.backend.domain.edu.curriculum.controller.dto.CurriculumResponse;
import ssafy.study.backend.domain.edu.curriculum.entity.Curriculum;
import ssafy.study.backend.domain.edu.curriculum.repository.CurriculumRepository;
import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.repository.StudyRepository;
import ssafy.study.backend.fixture.CurriculumFixture;
import ssafy.study.backend.fixture.StudyFixture;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class CurriculumServiceTest {

	@InjectMocks
	private CurriculumService curriculumService;

	@Mock
	private CurriculumRepository curriculumRepository;

	@Mock
	private StudyRepository studyRepository;

	@Test
	@DisplayName("커리큘럼 생성 성공")
	void 커리큘럼_생성_성공() {
		// given
		Study study = StudyFixture.study(1L);
		CurriculumRequest request = new CurriculumRequest("1주차 - 스프링 입문", "스프링 기초 학습", 1);
		Curriculum curriculum = CurriculumFixture.curriculum(1L, study);
		given(studyRepository.findById(1L)).willReturn(Optional.of(study));
		given(curriculumRepository.save(any(Curriculum.class))).willReturn(curriculum);

		// when
		CurriculumResponse result = curriculumService.createCurriculums(1L, request);

		// then
		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.title()).isEqualTo(curriculum.getName());
	}

	@Test
	@DisplayName("커리큘럼 생성 실패 - 존재하지 않는 스터디")
	void 커리큘럼_생성_실패_존재하지_않는_스터디() {
		// given
		CurriculumRequest request = new CurriculumRequest("1주차", "설명", 1);
		given(studyRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> curriculumService.createCurriculums(999L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_NOT_FOUND);
	}

	@Test
	@DisplayName("커리큘럼 단건 조회 성공")
	void 커리큘럼_단건_조회_성공() {
		// given
		Study study = StudyFixture.study(1L);
		Curriculum curriculum = CurriculumFixture.curriculum(1L, study);
		given(curriculumRepository.findById(1L)).willReturn(Optional.of(curriculum));

		// when
		CurriculumResponse result = curriculumService.getCurriculums(1L);

		// then
		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.title()).isEqualTo(curriculum.getName());
	}

	@Test
	@DisplayName("커리큘럼 단건 조회 실패 - 존재하지 않는 커리큘럼")
	void 커리큘럼_단건_조회_실패_존재하지_않는_커리큘럼() {
		// given
		given(curriculumRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> curriculumService.getCurriculums(999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CURRICULUM_NOT_FOUND);
	}

	@Test
	@DisplayName("커리큘럼 삭제 성공")
	void 커리큘럼_삭제_성공() {
		// given
		willDoNothing().given(curriculumRepository).deleteById(1L);

		// when & then
		assertThatNoException().isThrownBy(() -> curriculumService.deleteCurriculums(1L));
		then(curriculumRepository).should().deleteById(1L);
	}

	@Test
	@DisplayName("커리큘럼 삭제 실패 - 존재하지 않는 커리큘럼")
	void 커리큘럼_삭제_실패_존재하지_않는_커리큘럼() {
		// given
		willThrow(new EmptyResultDataAccessException(1)).given(curriculumRepository).deleteById(999L);

		// when & then
		assertThatThrownBy(() -> curriculumService.deleteCurriculums(999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CURRICULUM_NOT_FOUND);
	}

	@Test
	@DisplayName("커리큘럼 수정 성공")
	void 커리큘럼_수정_성공() {
		// given
		Study study = StudyFixture.study(1L);
		Curriculum curriculum = CurriculumFixture.curriculum(1L, study);
		CurriculumRequest request = new CurriculumRequest("수정된 이름", "수정된 설명", 2);
		given(curriculumRepository.findById(1L)).willReturn(Optional.of(curriculum));

		// when
		CurriculumResponse result = curriculumService.updateCurriculums(1L, request);

		// then
		assertThat(result.title()).isEqualTo("수정된 이름");
		assertThat(result.description()).isEqualTo("수정된 설명");
		assertThat(result.order()).isEqualTo(2);
	}

	@Test
	@DisplayName("스터디별 커리큘럼 목록 조회 성공")
	void 스터디별_커리큘럼_목록_조회_성공() {
		// given
		Study study = StudyFixture.study(1L);
		Curriculum curriculum1 = CurriculumFixture.curriculum(1L, study);
		Curriculum curriculum2 = CurriculumFixture.curriculum(2L, study);
		PageRequest pageable = PageRequest.of(0, 10);
		Page<Curriculum> curriculumPage = new PageImpl<>(List.of(curriculum1, curriculum2), pageable, 2);
		given(studyRepository.existsById(1L)).willReturn(true);
		given(curriculumRepository.findByStudyId(1L, pageable)).willReturn(curriculumPage);

		// when
		Page<CurriculumResponse> result = curriculumService.getCurriculumsByStudy(1L, pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getContent()).hasSize(2);
	}

	@Test
	@DisplayName("스터디별 커리큘럼 목록 조회 실패 - 존재하지 않는 스터디")
	void 스터디별_커리큘럼_목록_조회_실패_존재하지_않는_스터디() {
		// given
		PageRequest pageable = PageRequest.of(0, 10);
		given(studyRepository.existsById(999L)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> curriculumService.getCurriculumsByStudy(999L, pageable))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_NOT_FOUND);
	}

	@Test
	@DisplayName("커리큘럼 수정 실패 - 존재하지 않는 커리큘럼")
	void 커리큘럼_수정_실패_존재하지_않는_커리큘럼() {
		// given
		CurriculumRequest request = new CurriculumRequest("수정된 이름", "수정된 설명", 2);
		given(curriculumRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> curriculumService.updateCurriculums(999L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CURRICULUM_NOT_FOUND);
	}
}
