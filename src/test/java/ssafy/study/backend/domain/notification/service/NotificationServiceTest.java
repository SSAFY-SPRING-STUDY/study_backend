package ssafy.study.backend.domain.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import ssafy.study.backend.domain.notification.controller.dto.response.NotificationResponse;
import ssafy.study.backend.domain.notification.entity.Notification;
import ssafy.study.backend.domain.notification.entity.NotificationRead;
import ssafy.study.backend.domain.notification.event.NotificationCreatedEvent;
import ssafy.study.backend.domain.notification.repository.NotificationReadRepository;
import ssafy.study.backend.domain.notification.repository.NotificationRepository;
import ssafy.study.backend.fixture.NotificationFixture;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private NotificationReadRepository notificationReadRepository;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	// ===================== 알림 생성 =====================

	@Test
	@DisplayName("알림 생성 성공 - 저장 후 이벤트 발행")
	void 알림_생성_성공() {
		// given
		Notification saved = NotificationFixture.notification(1L);
		given(notificationRepository.save(any(Notification.class))).willReturn(saved);
		willDoNothing().given(eventPublisher).publishEvent(any(NotificationCreatedEvent.class));

		// when
		assertThatNoException().isThrownBy(() -> notificationService.createNotification("테스트 알림 내용"));

		// then
		then(notificationRepository).should().save(any(Notification.class));
		then(eventPublisher).should().publishEvent(any(NotificationCreatedEvent.class));
	}

	// ===================== 알림 목록 조회 =====================

	@Test
	@DisplayName("알림 목록 조회 성공 - 읽음 여부 포함")
	void 알림_목록_조회_성공() {
		// given
		Notification n1 = NotificationFixture.notification(1L);
		Notification n2 = NotificationFixture.notification(2L);
		PageRequest pageable = PageRequest.of(0, 10);
		Page<Notification> notificationPage = new PageImpl<>(List.of(n1, n2), pageable, 2);

		given(notificationReadRepository.findReadNotificationIdsByMemberId(1L)).willReturn(Set.of(1L));
		given(notificationRepository.findAll(pageable)).willReturn(notificationPage);

		// when
		Page<NotificationResponse> result = notificationService.getNotifications(1L, pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getContent().get(0).isRead()).isTrue();
		assertThat(result.getContent().get(1).isRead()).isFalse();
	}

	// ===================== 알림 읽음 처리 =====================

	@Test
	@DisplayName("알림 읽음 처리 성공")
	void 알림_읽음_처리_성공() {
		// given
		given(notificationRepository.existsById(1L)).willReturn(true);
		given(notificationReadRepository.existsByNotificationIdAndMemberId(1L, 1L)).willReturn(false);
		given(notificationReadRepository.save(any(NotificationRead.class)))
			.willReturn(NotificationFixture.notificationRead(1L, 1L));

		// when
		assertThatNoException().isThrownBy(() -> notificationService.markAsRead(1L, 1L));

		// then
		then(notificationReadRepository).should().save(any(NotificationRead.class));
	}

	@Test
	@DisplayName("알림 읽음 처리 - 이미 읽은 경우 중복 저장하지 않음")
	void 알림_읽음_처리_이미_읽은_경우() {
		// given
		given(notificationRepository.existsById(1L)).willReturn(true);
		given(notificationReadRepository.existsByNotificationIdAndMemberId(1L, 1L)).willReturn(true);

		// when
		assertThatNoException().isThrownBy(() -> notificationService.markAsRead(1L, 1L));

		// then
		then(notificationReadRepository).should(never()).save(any(NotificationRead.class));
	}

	@Test
	@DisplayName("알림 읽음 처리 실패 - 존재하지 않는 알림")
	void 알림_읽음_처리_실패_존재하지_않는_알림() {
		// given
		given(notificationRepository.existsById(999L)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> notificationService.markAsRead(1L, 999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND);
	}
}
