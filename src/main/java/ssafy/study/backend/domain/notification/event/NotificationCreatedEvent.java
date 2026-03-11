package ssafy.study.backend.domain.notification.event;

import ssafy.study.backend.domain.notification.entity.Notification;

public record NotificationCreatedEvent(Notification notification) {
}
