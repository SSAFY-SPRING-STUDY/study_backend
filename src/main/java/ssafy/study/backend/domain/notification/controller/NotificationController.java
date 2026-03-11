package ssafy.study.backend.domain.notification.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.notification.controller.dto.response.NotificationResponse;
import ssafy.study.backend.domain.notification.service.NotificationPushService;
import ssafy.study.backend.domain.notification.service.NotificationService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

	private final NotificationService notificationService;
	private final NotificationPushService notificationPushService;

	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(@AuthenticationPrincipal Long memberId) {
		return notificationPushService.subscribe(memberId);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Page<NotificationResponse>> getNotifications(@AuthenticationPrincipal Long memberId, Pageable pageable) {
		Page<NotificationResponse> notifications = notificationService.getNotifications(memberId, pageable);
		return ApiResponse.success("알림 목록이 성공적으로 조회되었습니다.", notifications);
	}


	@PatchMapping("/{notificationId}/read")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> markAsRead(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long notificationId
	) {
		notificationService.markAsRead(memberId, notificationId);
		return ApiResponse.success("알림이 성공적으로 읽음 처리되었습니다.", null);
	}
}
