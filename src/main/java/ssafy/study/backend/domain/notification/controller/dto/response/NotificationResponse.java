package ssafy.study.backend.domain.notification.controller.dto.response;

import java.time.LocalDateTime;

import ssafy.study.backend.domain.notification.entity.Notification;

public record NotificationResponse(
	Long id,
	String content,
	Boolean isRead,
	LocalDateTime createdAt
) {
	public static NotificationResponse from(Notification notification, boolean isRead) {
		return new NotificationResponse(
			notification.getId(),
			notification.getContent(),
			isRead,
			notification.getCreatedAt()
		);
	}
}
