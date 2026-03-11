package ssafy.study.backend.domain.edu.curriculum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.edu.curriculum.entity.Curriculum;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {
	boolean existsById(Long curriculumId);
	Page<Curriculum> findByStudyId(Long studyId, Pageable pageable);
}
