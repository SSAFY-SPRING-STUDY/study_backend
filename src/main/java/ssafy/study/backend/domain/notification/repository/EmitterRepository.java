package ssafy.study.backend.domain.notification.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class EmitterRepository {

	// memberId -> (emitterId -> emitter)
	private final Map<Long, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();

	/**
	 * emitter 저장
	 */
	public void save(Long memberId, String emitterId, SseEmitter emitter) {
		emitters
			.computeIfAbsent(memberId, id -> new ConcurrentHashMap<>())
			.put(emitterId, emitter);
	}

	/**
	 * 특정 회원의 모든 emitter 조회
	 */
	public Map<String, SseEmitter> findAllByMemberId(Long memberId) {
		return emitters.getOrDefault(memberId, new ConcurrentHashMap<>());
	}

	/**
	 * 특정 emitter 삭제
	 */
	public void deleteById(Long memberId, String emitterId) {

		Map<String, SseEmitter> memberEmitters = emitters.get(memberId);

		if (memberEmitters == null) {
			return;
		}

		memberEmitters.remove(emitterId);

		// emitter가 하나도 없으면 memberId map 제거
		if (memberEmitters.isEmpty()) {
			emitters.remove(memberId);
		}
	}

	/**
	 * 모든 emitter 조회 (heartbeat 용)
	 */
	public Map<Long, Map<String, SseEmitter>> findAll() {
		return emitters;
	}
}
