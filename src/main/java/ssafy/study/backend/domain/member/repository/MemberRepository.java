package ssafy.study.backend.domain.member.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ssafy.study.backend.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByEmail(String email);
	boolean existsByEmail(String email);
	boolean existsByNickname(String nickname);

	@Query("SELECT m FROM Member m WHERE :keyword IS NULL OR m.email LIKE %:keyword% OR m.nickname LIKE %:keyword%")
	Page<Member> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
