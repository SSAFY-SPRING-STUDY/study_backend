package ssafy.study.backend.global.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.member.entity.MemberRole;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Component
@RequiredArgsConstructor
public class JwtProvider {

	private final JwtProperties jwtProperties;

    /* =======================
       Access Token
       ======================= */

	public String generateAccessToken(Long memberId, MemberRole role) {
		return generateToken(memberId, role, jwtProperties.access());
	}

	public Claims validateAccessToken(String token) {
		return parseToken(token, jwtProperties.access());
	}

    /* =======================
       Refresh Token
       ======================= */

	public String generateRefreshToken(Long memberId, MemberRole role) {
		return generateToken(memberId, role, jwtProperties.refresh());
	}

	public Claims validateRefreshToken(String token) {
		return parseToken(token, jwtProperties.refresh());
	}

	// 만료 여부와 무관하게 refresh token에서 memberId 추출 (로그아웃용)
	public Long extractMemberIdIgnoreExpiry(String token) {
		try {
			return Long.valueOf(
				Jwts.parser()
					.verifyWith(getKey(jwtProperties.refresh().secret()))
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getSubject()
			);
		} catch (ExpiredJwtException e) {
			return Long.valueOf(e.getClaims().getSubject());
		} catch (JwtException | IllegalArgumentException e) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}

    /* =======================
       Internal Logic
       ======================= */

	private String generateToken(Long memberId, MemberRole role, JwtProperties.TokenConfig config) {
		Instant now = Instant.now();
		Instant expiry = now.plusMillis(config.expiration());

		return Jwts.builder()
			.subject(memberId.toString())
			.claim("role", role.name())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiry))
			.signWith(getKey(config.secret()))
			.compact();
	}

	private Claims parseToken(String token, JwtProperties.TokenConfig config) {
		try {
			return Jwts.parser()
				.verifyWith(getKey(config.secret()))
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException e) {
			throw new CustomException(ErrorCode.TOKEN_EXPIRED);
		} catch (JwtException | IllegalArgumentException e) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}

	private SecretKey getKey(String secret) {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}
}
