package ssafy.study.backend.domain.auth.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.auth.config.PasswordResetProperties;

@Repository
@RequiredArgsConstructor
public class PasswordResetTokenRepository {

	private static final String PREFIX = "pwreset:token:";

	private final RedisTemplate<String, String> redisTemplate;
	private final PasswordResetProperties properties;

	public void save(String token, Long memberId) {
		Duration ttl = Duration.ofMillis(properties.tokenTtl());
		redisTemplate.opsForValue().set(generateKey(token), String.valueOf(memberId), ttl);
	}

	public Long findMemberIdAndDelete(String token) {
		String key = generateKey(token);
		String value = redisTemplate.opsForValue().getAndDelete(key);
		if (value == null) {
			return null;
		}
		return Long.valueOf(value);
	}

	private String generateKey(String token) {
		return PREFIX + token;
	}
}
