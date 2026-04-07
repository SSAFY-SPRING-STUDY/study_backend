package ssafy.study.backend.domain.edu.assignment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.edu.assignment.entity.AssignmentProgress;
import ssafy.study.backend.domain.edu.assignment.entity.AssignmentProgressStatus;

public interface AssignmentProgressRepository extends JpaRepository<AssignmentProgress, Long> {

	Optional<AssignmentProgress> findByStudyMemberIdAndAssignmentId(Long studyMemberId, Long assignmentId);

	boolean existsByStudyMemberIdAndAssignmentId(Long studyMemberId, Long assignmentId);

	List<AssignmentProgress> findByStudyMemberId(Long studyMemberId);

	boolean existsByStudyMemberIdAndAssignment_OrderInStudyLessThanAndStatusNot(
		Long studyMemberId, int orderInStudy, AssignmentProgressStatus status);
}
