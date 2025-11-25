package com.linkly.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

import com.linkly.auth.dto.LoginRequest;
import com.linkly.auth.dto.LoginResponse;
import com.linkly.auth.dto.SignupRequest;
import com.linkly.domain.AppUser;
import com.linkly.domain.enums.UserRole;
import com.linkly.global.config.JwtTokenProvider;
import com.linkly.global.exception.ResourceNotFoundException;
import com.linkly.global.security.SecurityUtils;
import com.linkly.user.AppUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

	@Mock
	private AppUserRepository appUserRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private AuthService authService;

	private MockedStatic<SecurityUtils> securityUtilsMock;

	@BeforeEach
	void setUp() {
		securityUtilsMock = mockStatic(SecurityUtils.class);
	}

	@AfterEach
	void tearDown() {
		securityUtilsMock.close();
	}

	@Test
	@DisplayName("회원가입 성공")
	void signup_Success() {
		// given
		SignupRequest request = new SignupRequest("test@example.com", "password123", "테스트 사용자");

		AppUser savedUser = AppUser.builder().id(1L).email("test@example.com").password("encodedPassword")
				.name("테스트 사용자").role(UserRole.USER).build();

		given(appUserRepository.existsByEmail("test@example.com")).willReturn(false);
		given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
		given(appUserRepository.save(any(AppUser.class))).willReturn(savedUser);
		given(jwtTokenProvider.generateToken(1L, "test@example.com")).willReturn("jwt-token");

		// when
		LoginResponse response = authService.signup(request);

		// then
		assertThat(response.getAccessToken()).isEqualTo("jwt-token");
		assertThat(response.getUserId()).isEqualTo(1L);
		assertThat(response.getEmail()).isEqualTo("test@example.com");
		assertThat(response.getName()).isEqualTo("테스트 사용자");

		then(appUserRepository).should(times(1)).existsByEmail("test@example.com");
		then(passwordEncoder).should(times(1)).encode("password123");
		then(appUserRepository).should(times(1)).save(any(AppUser.class));
		then(jwtTokenProvider).should(times(1)).generateToken(1L, "test@example.com");
	}

	@Test
	@DisplayName("회원가입 실패 - 이메일 중복")
	void signup_DuplicateEmail() {
		// given
		SignupRequest request = new SignupRequest("duplicate@example.com", "password123", "테스트 사용자");

		given(appUserRepository.existsByEmail("duplicate@example.com")).willReturn(true);

		// when & then
		assertThatThrownBy(() -> authService.signup(request)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("이미 사용 중인 이메일입니다.");

		then(appUserRepository).should(times(1)).existsByEmail("duplicate@example.com");
		then(passwordEncoder).should(never()).encode(anyString());
		then(appUserRepository).should(never()).save(any(AppUser.class));
	}

	@Test
	@DisplayName("로그인 성공")
	void login_Success() {
		// given
		LoginRequest request = new LoginRequest("test@example.com", "password123");

		AppUser user = AppUser.builder().id(1L).email("test@example.com").password("encodedPassword").name("테스트 사용자")
				.role(UserRole.USER).build();

		given(appUserRepository.findByEmailAndDeletedAtIsNull("test@example.com")).willReturn(Optional.of(user));
		given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
		given(jwtTokenProvider.generateToken(1L, "test@example.com")).willReturn("jwt-token");

		// when
		LoginResponse response = authService.login(request);

		// then
		assertThat(response.getAccessToken()).isEqualTo("jwt-token");
		assertThat(response.getUserId()).isEqualTo(1L);
		assertThat(response.getEmail()).isEqualTo("test@example.com");

		then(appUserRepository).should(times(1)).findByEmailAndDeletedAtIsNull("test@example.com");
		then(passwordEncoder).should(times(1)).matches("password123", "encodedPassword");
		then(jwtTokenProvider).should(times(1)).generateToken(1L, "test@example.com");
	}

	@Test
	@DisplayName("로그인 실패 - 존재하지 않는 이메일")
	void login_EmailNotFound() {
		// given
		LoginRequest request = new LoginRequest("notfound@example.com", "password123");

		given(appUserRepository.findByEmailAndDeletedAtIsNull("notfound@example.com")).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> authService.login(request)).isInstanceOf(BadCredentialsException.class)
				.hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

		then(appUserRepository).should(times(1)).findByEmailAndDeletedAtIsNull("notfound@example.com");
		then(passwordEncoder).should(never()).matches(anyString(), anyString());
	}

	@Test
	@DisplayName("로그인 실패 - 비밀번호 불일치")
	void login_WrongPassword() {
		// given
		LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

		AppUser user = AppUser.builder().id(1L).email("test@example.com").password("encodedPassword").name("테스트 사용자")
				.role(UserRole.USER).build();

		given(appUserRepository.findByEmailAndDeletedAtIsNull("test@example.com")).willReturn(Optional.of(user));
		given(passwordEncoder.matches("wrongpassword", "encodedPassword")).willReturn(false);

		// when & then
		assertThatThrownBy(() -> authService.login(request)).isInstanceOf(BadCredentialsException.class)
				.hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

		then(appUserRepository).should(times(1)).findByEmailAndDeletedAtIsNull("test@example.com");
		then(passwordEncoder).should(times(1)).matches("wrongpassword", "encodedPassword");
		then(jwtTokenProvider).should(never()).generateToken(any(), anyString());
	}

	@Test
	@DisplayName("회원 탈퇴 성공")
	void withdraw_Success() {
		// given
		Long currentUserId = 1L;
		AppUser user = AppUser.builder().id(currentUserId).email("test@example.com").password("encodedPassword")
				.name("테스트 사용자").role(UserRole.USER).build();

		securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
		given(appUserRepository.findByIdAndDeletedAtIsNull(currentUserId)).willReturn(Optional.of(user));

		// when
		authService.withdraw();

		// then
		assertThat(user.getDeletedAt()).isNotNull();
		assertThat(user.isDeleted()).isTrue();

		securityUtilsMock.verify(SecurityUtils::getCurrentUserId, times(1));
		then(appUserRepository).should(times(1)).findByIdAndDeletedAtIsNull(currentUserId);
	}

	@Test
	@DisplayName("회원 탈퇴 실패 - 존재하지 않는 사용자")
	void withdraw_UserNotFound() {
		// given
		Long currentUserId = 999L;

		securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
		given(appUserRepository.findByIdAndDeletedAtIsNull(currentUserId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> authService.withdraw()).isInstanceOf(ResourceNotFoundException.class);

		securityUtilsMock.verify(SecurityUtils::getCurrentUserId, times(1));
		then(appUserRepository).should(times(1)).findByIdAndDeletedAtIsNull(currentUserId);
	}

	@Test
	@DisplayName("회원 탈퇴 실패 - 이미 삭제된 사용자")
	void withdraw_AlreadyDeleted() {
		// given
		Long currentUserId = 1L;

		securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
		given(appUserRepository.findByIdAndDeletedAtIsNull(currentUserId)).willReturn(Optional.empty()); // 이미 삭제된 사용자

		// when & then
		assertThatThrownBy(() -> authService.withdraw()).isInstanceOf(ResourceNotFoundException.class);

		securityUtilsMock.verify(SecurityUtils::getCurrentUserId, times(1));
		then(appUserRepository).should(times(1)).findByIdAndDeletedAtIsNull(currentUserId);
	}
}
