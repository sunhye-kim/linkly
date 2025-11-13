package com.linkly.auth;

import com.linkly.domain.AppUser;
import com.linkly.user.AppUserRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final AppUserRepository appUserRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		AppUser appUser = appUserRepository.findByEmailAndDeletedAtIsNull(email)
				.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

		return User.builder().username(appUser.getEmail()).password(appUser.getPassword())
				.authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name())))
				.build();
	}

	// ID로 사용자 조회 (JWT 인증에서 사용)
	@Transactional(readOnly = true)
	public AppUser loadUserById(Long id) {
		return appUserRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + id));
	}
}
