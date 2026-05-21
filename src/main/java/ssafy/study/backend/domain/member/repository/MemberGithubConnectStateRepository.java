package ssafy.study.backend.domain.member.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * GitHub 계정 연결 흐름의 state 토큰 저장소.
 * <p>
 * state 는 CSRF 방어 + "어느 회원에 묶을지" 식별 두 가지 역할을 한다.
 * 짧은 TTL 로 발급하여 콜백에서 즉시 소비·삭제한다.
 */
@Repository
@RequiredArgsConstructor
public class MemberGithubConnectStateRepository {

	private static final String PREFIX = "github:connect:state:";
	private static final Duration TTL = Duration.ofMinutes(5);

	private final RedisTemplate<String, String> redisTemplate;

	public void save(String state, Long memberId) {
		redisTemplate.opsForValue().set(generateKey(state), String.valueOf(memberId), TTL);
	}

	public Long findMemberIdAndDelete(String state) {
		String value = redisTemplate.opsForValue().getAndDelete(generateKey(state));
		if (value == null) {
			return null;
		}
		return Long.valueOf(value);
	}

	private String generateKey(String state) {
		return PREFIX + state;
	}
}
