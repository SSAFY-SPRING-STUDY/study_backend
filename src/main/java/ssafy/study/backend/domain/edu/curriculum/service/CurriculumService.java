package ssafy.study.backend.domain.study.curriculum.service;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.study.curriculum.controller.dto.CurriculumRequest;
import ssafy.study.backend.domain.study.curriculum.controller.dto.CurriculumResponse;
import ssafy.study.backend.domain.study.curriculum.entity.Curriculum;
import ssafy.study.backend.domain.study.curriculum.entity.DifficultyLevel;
import ssafy.study.backend.domain.study.curriculum.repository.CurriculumRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CurriculumService {
	private final CurriculumRepository curriculumRepository;

	@Transactional
	public CurriculumResponse createCurriculums(CurriculumRequest request) {
		Curriculum curriculum = Curriculum.builder()
			.level(request.level())
			.name(request.name())
			.description(request.description())
			.orderInStudy(request.order())
			.build();

		Curriculum savedCurriculum = curriculumRepository.save(curriculum);

		return CurriculumResponse.from(savedCurriculum);
	}

	public CurriculumResponse getCurriculums(Long curriculumId) {
		Curriculum curriculum = curriculumRepository.findById(curriculumId)
			.orElseThrow(() -> new CustomException(ErrorCode.CURRICULUM_NOT_FOUND));

		return CurriculumResponse.from(curriculum);
	}

	@Transactional
	public void deleteCurriculums(Long curriculumId) {
		try {
			curriculumRepository.deleteById(curriculumId);
		} catch (EmptyResultDataAccessException e) {
			throw new CustomException(ErrorCode.CURRICULUM_NOT_FOUND);
		}
	}

	@Transactional
	public CurriculumResponse updateCurriculums(Long curriculumId, CurriculumRequest request) {
		Curriculum curriculum = curriculumRepository.findById(curriculumId)
			.orElseThrow(() -> new CustomException(ErrorCode.CURRICULUM_NOT_FOUND));

		curriculum.update(request.name(), request.description(), request.order());
		return CurriculumResponse.from(curriculum);
	}

	public List<CurriculumResponse> getCurriculumsByLevel(DifficultyLevel level) {
		List<CurriculumResponse> curriculums = curriculumRepository.findByLevel(level).stream()
			.map(CurriculumResponse::from)
			.toList();
	}
}
