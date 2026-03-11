package ssafy.study.backend.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ssafy.study.backend.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
