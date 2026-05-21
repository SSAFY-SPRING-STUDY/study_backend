package ssafy.study.backend.domain.member.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.member.entity.MemberProfile;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {

	Optional<MemberProfile> findByMemberId(Long memberId);

	List<MemberProfile> findAllByMemberIdIn(Collection<Long> memberIds);
}
