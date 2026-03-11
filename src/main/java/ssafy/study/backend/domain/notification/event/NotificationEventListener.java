package ssafy.study.backend.domain.notification.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.notification.service.NotificationPushService;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

	private final NotificationPushService notificationPushService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleNotification(NotificationCreatedEvent event) {
		notificationPushService.sendAll(event.notification());
	}
}
