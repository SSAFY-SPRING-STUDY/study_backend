package ssafy.study.backend.domain.edu.curriculum.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.edu.curriculum.entity.Curriculum;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {
	boolean existsById(Long curriculumId);
}
