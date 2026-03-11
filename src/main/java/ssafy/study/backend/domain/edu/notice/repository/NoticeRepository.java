package ssafy.study.backend.domain.edu.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.edu.notice.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
