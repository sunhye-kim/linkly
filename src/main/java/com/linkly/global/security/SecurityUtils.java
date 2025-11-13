package com.linkly.global.security;

import com.linkly.domain.AppUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 인증 정보를 쉽게 조회하기 위한 유틸리티 클래스
 */
public class SecurityUtils {

	private SecurityUtils() {
		// 유틸리티 클래스는 인스턴스화 방지
		throw new IllegalStateException("Utility class");
	}

	/**
	 * 현재 인증된 사용자 정보를 가져옵니다.
	 *
	 * @return 현재 인증된 AppUser 객체
	 * @throws IllegalStateException
	 *             인증되지 않은 사용자이거나 Principal이 AppUser가 아닌 경우
	 */
	public static AppUser getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			throw new IllegalStateException("인증되지 않은 사용자입니다.");
		}

		Object principal = authentication.getPrincipal();
		if (!(principal instanceof AppUser)) {
			throw new IllegalStateException("인증 정보가 올바르지 않습니다.");
		}

		return (AppUser) principal;
	}

	/**
	 * 현재 인증된 사용자의 ID를 가져옵니다.
	 *
	 * @return 현재 인증된 사용자의 ID
	 * @throws IllegalStateException
	 *             인증되지 않은 사용자인 경우
	 */
	public static Long getCurrentUserId() {
		return getCurrentUser().getId();
	}

	/**
	 * 현재 인증된 사용자의 이메일을 가져옵니다.
	 *
	 * @return 현재 인증된 사용자의 이메일
	 * @throws IllegalStateException
	 *             인증되지 않은 사용자인 경우
	 */
	public static String getCurrentUserEmail() {
		return getCurrentUser().getEmail();
	}

	/**
	 * 현재 사용자가 인증되어 있는지 확인합니다.
	 *
	 * @return 인증되어 있으면 true, 아니면 false
	 */
	public static boolean isAuthenticated() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken);
	}
}
