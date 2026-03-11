package ssafy.study.backend.domain.notification.service;

import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.notification.repository.EmitterRepository;

@Service
@RequiredArgsConstructor
public class NotificationPushService {

	private final EmitterRepository emitterRepository;

	private static final long TIMEOUT = Long.MAX_VALUE;
	private static final int MAX_EMITTER_PER_USER = 5;

	public void send(Long memberId, Object data) {

		Map<String, SseEmitter> emitters =
			emitterRepository.findAllByMemberId(memberId);

		emitters.forEach((id, emitter) ->
			sendEvent(memberId, id, emitter, "notification", data)
		);
	}

	public SseEmitter subscribe(Long memberId) {

		Map<String, SseEmitter> emitters =
			emitterRepository.findAllByMemberId(memberId);

		// emitter limit
		if (emitters.size() >= MAX_EMITTER_PER_USER) {

			emitters.keySet().stream()
				.min(String::compareTo)
				.ifPresent(oldestId ->
					emitterRepository.deleteById(memberId, oldestId)
				);
		}

		String emitterId = memberId + "_" + System.currentTimeMillis();

		SseEmitter emitter = new SseEmitter(TIMEOUT);

		emitterRepository.save(memberId, emitterId, emitter);

		emitter.onCompletion(() ->
			emitterRepository.deleteById(memberId, emitterId)
		);

		emitter.onTimeout(() ->
			emitterRepository.deleteById(memberId, emitterId)
		);

		emitter.onError(e ->
			emitterRepository.deleteById(memberId, emitterId)
		);

		// connect event
		sendEvent(memberId, emitterId, emitter, "connect", "connected");

		return emitter;
	}

	private void sendEvent(
		Long memberId,
		String emitterId,
		SseEmitter emitter,
		String event,
		Object data
	) {

		try {

			emitter.send(
				SseEmitter.event()
					.name(event)
					.data(data)
			);

		} catch (Exception e) {

			emitterRepository.deleteById(memberId, emitterId);
		}
	}
	/**
	 * 모든 구독자에게 이벤트 발송
	 */
	public void sendAll(Object data) {
		emitterRepository.findAll().forEach((memberId, emitters) ->
			emitters.forEach((emitterId, emitter) ->
				sendEvent(memberId, emitterId, emitter, "notification", data)
			)
		);
	}

	@Scheduled(fixedDelay = 60000)
	public void heartbeat() {

		emitterRepository.findAll().forEach((memberId, emitters) ->
			emitters.forEach((emitterId, emitter) ->
				sendEvent(memberId, emitterId, emitter, "ping", "")
			));
	}
}
