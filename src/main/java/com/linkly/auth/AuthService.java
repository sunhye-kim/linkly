package com.linkly.auth;

import com.linkly.auth.dto.LoginRequest;
import com.linkly.auth.dto.LoginResponse;
import com.linkly.auth.dto.SignupRequest;
import com.linkly.domain.AppUser;
import com.linkly.domain.enums.UserRole;
import com.linkly.global.config.JwtTokenProvider;
import com.linkly.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * 회원가입
	 */
	@Transactional
	public LoginResponse signup(SignupRequest request) {
		// 이메일 중복 확인
		if (appUserRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}

		// 사용자 생성
		AppUser appUser =
				AppUser.builder()
						.email(request.getEmail())
						.password(passwordEncoder.encode(request.getPassword()))
						.name(request.getName())
						.role(UserRole.USER)
						.build();

		AppUser savedUser = appUserRepository.save(appUser);

		// JWT 토큰 생성
		String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail());

		return new LoginResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getName());
	}

	/**
	 * 로그인
	 */
	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		// 사용자 조회
		AppUser appUser =
				appUserRepository
						.findByEmailAndDeletedAtIsNull(request.getEmail())
						.orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

		// 비밀번호 확인
		if (!passwordEncoder.matches(request.getPassword(), appUser.getPassword())) {
			throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
		}

		// JWT 토큰 생성
		String token = jwtTokenProvider.generateToken(appUser.getId(), appUser.getEmail());

		return new LoginResponse(token, appUser.getId(), appUser.getEmail(), appUser.getName());
	}
}