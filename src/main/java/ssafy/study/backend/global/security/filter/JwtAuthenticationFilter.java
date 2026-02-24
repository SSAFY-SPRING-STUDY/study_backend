package ssafy.study.backend.global.security.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ssafy.study.backend.global.cookie.CookieService;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.security.jwt.JwtProvider;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;
	private final CookieService cookieService;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain)
		throws ServletException, IOException {

		String accessToken = cookieService.extractAccessToken(request);

		if (accessToken != null) {
			try {
				Claims claims = jwtProvider.validateAccessToken(accessToken);

				Long memberId = Long.valueOf(claims.getSubject());
				String role = claims.get("role", String.class);

				setAuthentication(memberId, role);

			} catch (CustomException e) {
				log.debug("JWT 인증 실패: {}", e.getErrorCode().getMessage());
			} catch (Exception e) {
				log.debug("JWT 파싱 중 예외 발생", e);
			}
		}

		filterChain.doFilter(request, response);
	}

	private void setAuthentication(Long memberId, String role) {

		Authentication authentication =
			new UsernamePasswordAuthenticationToken(
				memberId,
				null,
				List.of(new SimpleGrantedAuthority(role))
			);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
