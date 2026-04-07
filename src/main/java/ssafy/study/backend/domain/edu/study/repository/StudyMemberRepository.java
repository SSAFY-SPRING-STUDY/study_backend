package ssafy.study.backend.domain.edu.study.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.edu.study.entity.StudyMember;
import ssafy.study.backend.domain.edu.study.entity.StudyMemberRole;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

	Optional<StudyMember> findByStudyIdAndMemberId(Long studyId, Long memberId);

	boolean existsByStudyIdAndMemberId(Long studyId, Long memberId);

	boolean existsByStudyIdAndMemberIdAndRole(Long studyId, Long memberId, StudyMemberRole role);

	List<StudyMember> findByStudyId(Long studyId);

	long countByStudyId(Long studyId);

	long countByStudyIdAndRole(Long studyId, StudyMemberRole role);
}
