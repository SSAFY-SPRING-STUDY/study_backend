package ssafy.study.backend.domain.notification.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ssafy.study.backend.domain.notification.entity.NotificationRead;

public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {

	@Query("SELECT nr.notificationId FROM NotificationRead nr WHERE nr.memberId = :memberId")
	Set<Long> findReadNotificationIdsByMemberId(@Param("memberId") Long memberId);

	boolean existsByNotificationIdAndMemberId(Long notificationId, Long memberId);
}
