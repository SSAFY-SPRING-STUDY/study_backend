package ssafy.study.backend.domain.edu.study.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.study.controller.dto.request.StudyRequest;
import ssafy.study.backend.domain.edu.study.controller.dto.request.StudyResponse;
import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.entity.StudyType;
import ssafy.study.backend.domain.edu.study.repository.StudyRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyService {
	private final StudyRepository studyRepository;

	@Transactional
	public StudyResponse createStudy(StudyRequest request) {
		Study study = Study.builder()
			.name(request.name())
			.description(request.description())
			.level(request.level())
			.type(request.type())
			.build();

		Study savedStudy = studyRepository.save(study);
		return StudyResponse.from(savedStudy);
	}

	@Transactional
	public StudyResponse updateStudy(Long studyId, StudyRequest request) {
		Study study = studyRepository.findById(studyId)
			.orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

		study.update(request.name(), request.description(), request.level(), request.type());
		return StudyResponse.from(study);
	}

	@Transactional
	public void deleteStudy(Long studyId) {
		studyRepository.deleteById(studyId);
	}

	public StudyResponse getStudy(Long studyId) {
		Study study = studyRepository.findById(studyId)
			.orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

		return StudyResponse.from(study);
	}

	public Page<StudyResponse> getStudiesByType(StudyType studyType, Pageable pageable) {
		Page<Study> studies = studyRepository.findByType(studyType, pageable);
		return studies.map(StudyResponse::from);
	}
}
