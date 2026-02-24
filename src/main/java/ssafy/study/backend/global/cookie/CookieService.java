package ssafy.study.backend.global.cookie;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.global.security.jwt.JwtProperties;

@Service
@RequiredArgsConstructor
public class CookieService {

	private final CookieProperties cookieProperties;
	private final JwtProperties jwtProperties;

	/* =========================
	   Public API (의미 단위)
	   ========================= */

	public void setAccessToken(HttpServletResponse response, String token) {
		setCookie(
			response,
			jwtProperties.access().cookieName(),
			token,
			Duration.ofMillis(jwtProperties.access().expiration())
		);
	}

	public void setRefreshToken(HttpServletResponse response, String token) {
		setCookie(
			response,
			jwtProperties.refresh().cookieName(),
			token,
			Duration.ofMillis(jwtProperties.refresh().expiration())
		);
	}

	public String extractAccessToken(HttpServletRequest request) {
		return extractCookie(
			request,
			jwtProperties.access().cookieName()
		);
	}

	public String extractRefreshToken(HttpServletRequest request) {
		return extractCookie(
			request,
			jwtProperties.refresh().cookieName()
		);
	}

	public void deleteAccessToken(HttpServletResponse response) {
		deleteCookie(response, jwtProperties.access().cookieName());
	}

	public void deleteRefreshToken(HttpServletResponse response) {
		deleteCookie(response, jwtProperties.refresh().cookieName());
	}

	/* =========================
	   Private (저수준)
	   ========================= */

	private void setCookie(
		HttpServletResponse response,
		String name,
		String value,
		Duration maxAge
	) {

		ResponseCookie cookie = ResponseCookie.from(name, value)
			.httpOnly(true)
			.secure(cookieProperties.secure())
			.path("/")
			.domain(cookieProperties.domain())
			.maxAge(maxAge)
			.sameSite(cookieProperties.sameSite())
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private String extractCookie(HttpServletRequest request, String name) {
		if (request.getCookies() == null) {
			return null;
		}

		for (Cookie cookie : request.getCookies()) {
			if (name.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	private void deleteCookie(HttpServletResponse response, String name) {
		setCookie(response, name, "", Duration.ZERO);
	}
}
