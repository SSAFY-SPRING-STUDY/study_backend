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
