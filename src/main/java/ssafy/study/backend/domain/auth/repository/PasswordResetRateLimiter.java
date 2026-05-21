package ssafy.study.backend.domain.auth.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.auth.config.PasswordResetProperties;

@Repository
@RequiredArgsConstructor
public class PasswordResetRateLimiter {

	private static final String PREFIX = "pwreset:ratelimit:";

	private final RedisTemplate<String, String> redisTemplate;
	private final PasswordResetProperties properties;

	/**
	 * 요청 카운터 증가. 한도 초과 시 false 반환.
	 */
	public boolean tryAcquire(String email) {
		String key = generateKey(email);
		Long count = redisTemplate.opsForValue().increment(key);
		if (count == null) {
			return false;
		}
		if (count == 1L) {
			redisTemplate.expire(key, Duration.ofMillis(properties.rateLimitWindow()));
		}
		return count <= properties.rateLimitMax();
	}

	private String generateKey(String email) {
		return PREFIX + email;
	}
}
