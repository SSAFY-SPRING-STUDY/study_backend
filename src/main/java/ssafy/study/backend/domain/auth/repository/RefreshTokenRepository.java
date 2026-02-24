package ssafy.study.backend.domain.auth.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.global.security.jwt.JwtProperties;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

	private static final String PREFIX = "refresh:";

	private final JwtProperties jwtProperties;
	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * 저장
	 */
	public void save(Long memberId, String refreshToken) {
		Duration ttl = Duration.ofMillis(jwtProperties.refresh().expiration());
		String key = generateKey(memberId);

		redisTemplate.opsForValue().set(
			key,
			refreshToken,
			ttl
		);
	}

	/**
	 * 조회
	 */
	public String findByMemberId(Long memberId) {
		String key = generateKey(memberId);
		return redisTemplate.opsForValue().get(key);
	}

	/**
	 * 삭제
	 */
	public void delete(Long memberId) {
		String key = generateKey(memberId);
		redisTemplate.delete(key);
	}

	private String generateKey(Long memberId) {
		return PREFIX + memberId;
	}
}
