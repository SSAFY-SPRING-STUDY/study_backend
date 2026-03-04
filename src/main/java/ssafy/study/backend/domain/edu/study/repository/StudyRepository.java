package ssafy.study.backend.domain.edu.study.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.entity.StudyType;

public interface StudyRepository extends JpaRepository<Study, Long> {
	Page<Study> findByType(StudyType studyType, Pageable pageable);
}
