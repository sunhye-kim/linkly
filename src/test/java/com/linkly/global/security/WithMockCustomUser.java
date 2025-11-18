package com.linkly.global.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * 테스트에서 인증된 사용자를 모킹하기 위한 커스텀 어노테이션
 *
 * 사용 예시:
 * <pre>
 * {@code
 * @Test
 * @WithMockCustomUser(userId = 1L, email = "test@example.com")
 * void testMethod() {
 *     // SecurityUtils.getCurrentUserId()가 1L을 반환
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

	/**
	 * 사용자 ID (기본값: 1L)
	 */
	long userId() default 1L;

	/**
	 * 사용자 이메일 (기본값: "test@example.com")
	 */
	String email() default "test@example.com";

	/**
	 * 사용자 이름 (기본값: "Test User")
	 */
	String name() default "Test User";

	/**
	 * 사용자 비밀번호 (기본값: "password123")
	 */
	String password() default "password123";
}