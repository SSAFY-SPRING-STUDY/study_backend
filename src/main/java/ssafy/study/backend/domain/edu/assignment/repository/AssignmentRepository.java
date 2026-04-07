package ssafy.study.backend.domain.edu.assignment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.edu.assignment.entity.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

	List<Assignment> findByStudyIdOrderByOrderInStudyAsc(Long studyId);

	List<Assignment> findByStudyIdAndOrderInStudyGreaterThan(Long studyId, int orderInStudy);
}
