package ssafy.study.backend.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.notification.entity.Notification;
import ssafy.study.backend.domain.notification.entity.NotificationRead;

public class NotificationFixture {

	public static Notification notification() {
		return Notification.create("테스트 알림 내용");
	}

	public static Notification notification(Long id) {
		Notification notification = Notification.create("테스트 알림 내용");
		ReflectionTestUtils.setField(notification, "id", id);
		return notification;
	}

	public static NotificationRead notificationRead(Long notificationId, Long memberId) {
		return NotificationRead.create(notificationId, memberId);
	}
}
