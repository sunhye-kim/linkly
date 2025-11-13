package com.linkly.global.security;

import com.linkly.auth.CustomUserDetailsService;
import com.linkly.domain.AppUser;
import com.linkly.global.config.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final CustomUserDetailsService customUserDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			// 요청에서 JWT 토큰 추출
			String jwt = getJwtFromRequest(request);

			// 토큰이 존재하고 유효한 경우
			if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
				// 토큰에서 사용자 ID 추출
				Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

				// 사용자 정보 조회
				AppUser appUser = customUserDetailsService.loadUserById(userId);

				// 인증 객체 생성
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(appUser,
						null,
						Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name())));

				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				// SecurityContext에 인증 정보 설정
				SecurityContextHolder.getContext().setAuthentication(authentication);

				log.debug("Security Context에 '{}' 인증 정보를 저장했습니다.", appUser.getEmail());
			}
		} catch (Exception ex) {
			log.error("Security Context에 사용자 인증을 설정할 수 없습니다.", ex);
		}

		// 다음 필터로 요청 전달
		filterChain.doFilter(request, response);
	}

	/**
	 * 요청 헤더에서 JWT 토큰 추출
	 */
	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");

		// Bearer 토큰 형식인지 확인
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7); // "Bearer " 이후의 토큰 문자열 반환
		}

		return null;
	}
}
