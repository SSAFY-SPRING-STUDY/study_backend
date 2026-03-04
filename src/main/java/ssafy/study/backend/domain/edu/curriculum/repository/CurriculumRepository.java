package ssafy.study.backend.domain.study.curriculum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.study.curriculum.entity.Curriculum;
import ssafy.study.backend.domain.study.curriculum.entity.DifficultyLevel;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {
	boolean existsById(Long curriculumId);
	Page<Curriculum> findByLevel(DifficultyLevel level, Pageable pageable);}
