package ssafy.study.backend.global.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import jakarta.servlet.DispatcherType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.global.security.exception.CustomAccessDeniedHandler;
import ssafy.study.backend.global.security.exception.CustomAuthenticationEntryPoint;
import ssafy.study.backend.global.security.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CorsConfigurationSource corsConfigurationSource;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			.sessionManagement(
				session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함 (JWT 방식)

			.authorizeHttpRequests(
				authorize -> authorize
					.dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
					.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

					// Swagger 관련 URL 접근 허용
					.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**",
						"/swagger-ui.html").permitAll()
					// 에러 페이지 접근 허용
					.requestMatchers("/error").permitAll()
					.requestMatchers("/actuator/**").permitAll()

					// IMAGE Domain - 마크다운 렌더링 시 <img> 태그가 인증 없이 접근
					.requestMatchers(HttpMethod.GET, "/api/v1/images/**").permitAll()

					// ADMIN Domain
					.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

					// MEMBER Domain
					.requestMatchers(HttpMethod.POST, "/api/v1/members/signup").permitAll()

					// AUTH Domain
					.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
					.requestMatchers(HttpMethod.GET, "/api/v1/auth/github").permitAll()
					.requestMatchers(HttpMethod.GET, "/api/v1/auth/github/callback").permitAll()
					// 비밀번호 재설정 — 로그인 불가능한 사용자가 호출하는 통로이므로 permitAll.
					// 남용 방지는 PasswordResetService 의 RateLimiter 와 User Enumeration 방지 로직이 담당.
					.requestMatchers(HttpMethod.POST, "/api/v1/auth/password-reset/request").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/v1/auth/password-reset/confirm").permitAll()

					// GitHub 계정 연결 콜백 — GitHub 이 우리 쿠키를 들고 오지 않으므로 JWT 인증 불가.
					// 대신 Redis 의 state 토큰으로 어느 회원에 묶을지 식별 + CSRF 방어.
					.requestMatchers(HttpMethod.GET, "/api/v1/members/me/github/connect/callback").permitAll()

					// STUDY Domain - 비회원 조회 허용
					.requestMatchers(HttpMethod.GET, "/api/v1/studies").permitAll()
					.requestMatchers(HttpMethod.GET, "/api/v1/studies/{studyId}").permitAll()

					// CURRICULUM Domain - 비회원 조회 허용
					.requestMatchers(HttpMethod.GET, "/api/v1/studies/{studyId}/curriculums").permitAll()
					.requestMatchers(HttpMethod.GET, "/api/v1/curriculums/{curriculumId}").permitAll()

					// POST Domain - 비회원 조회 허용
					.requestMatchers(HttpMethod.GET, "/api/v1/curriculums/{curriculumId}/posts").permitAll()
					.requestMatchers(HttpMethod.GET, "/api/v1/posts/{postId}").permitAll()

					// COMMENT Domain - 읽기만 허용 (쓰기는 인증 필수)
					.requestMatchers(HttpMethod.GET, "/api/v1/posts/{postId}/comments").permitAll()
					.requestMatchers(HttpMethod.GET, "/api/v1/comments/{commentId}/recomments").permitAll()

					// NOTICE Domain - 비회원 조회 허용
					.requestMatchers(HttpMethod.GET, "/api/v1/notices").permitAll()
					.requestMatchers(HttpMethod.GET, "/api/v1/notices/{noticeId}").permitAll()

					// MEMBER Domain - 다른 사용자 프로필 조회 허용 (/me 는 인증 필요하므로 별도 분기)
					.requestMatchers(HttpMethod.GET, "/api/v1/members/{memberId:[0-9]+}").permitAll()

					.anyRequest().authenticated()
			)


			// ✅ JWT 필터 등록
			.addFilterBefore(
				jwtAuthenticationFilter,
				UsernamePasswordAuthenticationFilter.class)

			// ✅ 기본 인증 방식 비활성화 (JWT 사용)
			.httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
			.formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화

			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(customAuthenticationEntryPoint)
				.accessDeniedHandler(customAccessDeniedHandler)
			);

		return http.build();
	}
}
