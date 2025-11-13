package com.linkly.global.config;

import com.linkly.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// CSRF 비활성화 (JWT 사용 시 필요)
				.csrf(AbstractHttpConfigurer::disable)

				// 세션 사용하지 않음 (Stateless)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// 요청에 대한 인증/인가 설정
				.authorizeHttpRequests(auth -> auth
						// 공개 엔드포인트 (인증 불필요)
						.requestMatchers("/auth/**", // 로그인, 회원가입
								"/health", // 헬스체크
								"/swagger-ui/**", // Swagger UI
								"/v3/api-docs/**", // OpenAPI 문서
								"/swagger-resources/**", "/webjars/**")
						.permitAll()

						// 나머지 모든 요청은 인증 필요
						.anyRequest().authenticated())

				// JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 이전에 실행)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
