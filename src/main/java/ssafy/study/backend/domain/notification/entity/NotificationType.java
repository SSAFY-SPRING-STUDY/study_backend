package ssafy.study.backend.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
	NOTICE_CREATED("공지를 확인해주세요!");

	private final String message;
}
