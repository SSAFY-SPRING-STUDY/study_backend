package ssafy.study.backend.domain.notification.service;

import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.notification.controller.dto.response.NotificationResponse;
import ssafy.study.backend.domain.notification.entity.Notification;
import ssafy.study.backend.domain.notification.entity.NotificationRead;
import ssafy.study.backend.domain.notification.event.NotificationCreatedEvent;
import ssafy.study.backend.domain.notification.repository.NotificationReadRepository;
import ssafy.study.backend.domain.notification.repository.NotificationRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final NotificationReadRepository notificationReadRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public void createNotification(String message) {
		Notification notification = notificationRepository.save(Notification.create(message));
		eventPublisher.publishEvent(new NotificationCreatedEvent(notification));
	}

	public Page<NotificationResponse> getNotifications(Long memberId, Pageable pageable) {
		Set<Long> readIds = notificationReadRepository.findReadNotificationIdsByMemberId(memberId);
		return notificationRepository.findAll(pageable)
			.map(n -> NotificationResponse.from(n, readIds.contains(n.getId())));
	}

	@Transactional
	public void markAsRead(Long memberId, Long notificationId) {
		if (!notificationRepository.existsById(notificationId)) {
			throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
		}
		if (!notificationReadRepository.existsByNotificationIdAndMemberId(notificationId, memberId)) {
			notificationReadRepository.save(NotificationRead.create(notificationId, memberId));
		}
	}
}
