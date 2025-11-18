package com.linkly.global.security;

import com.linkly.domain.AppUser;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * @WithMockCustomUser 어노테이션을 위한 SecurityContext Factory
 *
 * 이 클래스는 테스트 실행 시 SecurityContext에 인증된 AppUser를 설정합니다.
 */
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

	@Override
	public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();

		// @WithMockCustomUser 어노테이션의 파라미터로 AppUser 생성
		AppUser principal = AppUser.builder()
				.id(annotation.userId())
				.email(annotation.email())
				.name(annotation.name())
				.password(annotation.password())
				.build();

		// Authentication 객체 생성 (권한은 빈 리스트로 설정)
		Authentication auth = new UsernamePasswordAuthenticationToken(
				principal,
				annotation.password(),
				Collections.emptyList()
		);

		context.setAuthentication(auth);
		return context;
	}
}