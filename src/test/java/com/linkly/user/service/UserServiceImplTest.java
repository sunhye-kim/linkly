package com.linkly.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

import com.linkly.domain.AppUser;
import com.linkly.global.exception.InvalidRequestException;
import com.linkly.global.exception.ResourceNotFoundException;
import com.linkly.user.AppUserRepository;
import com.linkly.user.UserServiceImpl;
import com.linkly.user.dto.CreateUserRequest;
import com.linkly.user.dto.UpdateUserRequest;
import com.linkly.user.dto.UserResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Service 계층 테스트 Django의 Service 테스트와 유사 @ExtendWith(MockitoExtension.class):
 * Mockito를 사용한 단위 테스트 - Repository를 Mock으로 대체 - 비즈니스 로직만 테스트 (DB 접근 없음) - 빠른 실행
 * 속도
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 테스트")
class UserServiceImplTest {

	@Mock
	private AppUserRepository userRepository;

	@InjectMocks
	private UserServiceImpl userService;

	@Test
	@DisplayName("회원 가입 성공")
	void createUser_Success() {
		// given
		CreateUserRequest request = CreateUserRequest.builder().email("test@example.com").password("password123")
				.name("테스트 사용자").build();

		AppUser savedUser = AppUser.builder().id(1L).email("test@example.com").password("password123").name("테스트 사용자")
				.build();

		given(userRepository.existsByEmail("test@example.com")).willReturn(false);
		given(userRepository.save(any(AppUser.class))).willReturn(savedUser);

		// when
		UserResponse response = userService.createUser(request);

		// then
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getEmail()).isEqualTo("test@example.com");
		assertThat(response.getName()).isEqualTo("테스트 사용자");

		then(userRepository).should(times(1)).existsByEmail("test@example.com");
		then(userRepository).should(times(1)).save(any(AppUser.class));
	}

	@Test
	@DisplayName("회원 가입 실패 - 이메일 중복")
	void createUser_DuplicateEmail() {
		// given
		CreateUserRequest request = CreateUserRequest.builder().email("duplicate@example.com").password("password123")
				.name("테스트 사용자").build();

		given(userRepository.existsByEmail("duplicate@example.com")).willReturn(true);

		// when & then
		assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(InvalidRequestException.class)
				.hasMessageContaining("이미 사용 중인 이메일입니다");

		then(userRepository).should(times(1)).existsByEmail("duplicate@example.com");
		then(userRepository).should(never()).save(any(AppUser.class));
	}

	@Test
	@DisplayName("회원 ID로 조회 성공")
	void getUserById_Success() {
		// given
		Long userId = 1L;
		AppUser user = AppUser.builder().id(userId).email("test@example.com").password("password123").name("테스트 사용자")
				.build();

		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

		// when
		UserResponse response = userService.getUserById(userId);

		// then
		assertThat(response.getId()).isEqualTo(userId);
		assertThat(response.getEmail()).isEqualTo("test@example.com");

		then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(userId);
	}

	@Test
	@DisplayName("회원 ID로 조회 실패 - 존재하지 않는 회원")
	void getUserById_NotFound() {
		// given
		Long userId = 999L;
		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.getUserById(userId)).isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("User");

		then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(userId);
	}

	@Test
	@DisplayName("이메일로 회원 조회 성공")
	void getUserByEmail_Success() {
		// given
		String email = "test@example.com";
		AppUser user = AppUser.builder().id(1L).email(email).password("password123").name("테스트 사용자").build();

		given(userRepository.findByEmailAndDeletedAtIsNull(email)).willReturn(Optional.of(user));

		// when
		UserResponse response = userService.getUserByEmail(email);

		// then
		assertThat(response.getEmail()).isEqualTo(email);

		then(userRepository).should(times(1)).findByEmailAndDeletedAtIsNull(email);
	}

	@Test
	@DisplayName("전체 회원 조회")
	void getAllUsers() {
		// given
		AppUser user1 = AppUser.builder().id(1L).email("user1@example.com").password("password123").name("사용자1")
				.build();

		AppUser user2 = AppUser.builder().id(2L).email("user2@example.com").password("password123").name("사용자2")
				.build();

		AppUser deletedUser = AppUser.builder().id(3L).email("deleted@example.com").password("password123")
				.name("삭제된 사용자").build();
		deletedUser.softDelete();

		given(userRepository.findAll()).willReturn(Arrays.asList(user1, user2, deletedUser));

		// when
		List<UserResponse> responses = userService.getAllUsers();

		// then
		assertThat(responses).hasSize(2); // 삭제된 사용자는 제외
		assertThat(responses).extracting("email").containsExactlyInAnyOrder("user1@example.com", "user2@example.com");

		then(userRepository).should(times(1)).findAll();
	}

	@Test
	@DisplayName("회원 정보 수정 성공")
	void updateUser_Success() {
		// given
		Long userId = 1L;
		UpdateUserRequest request = UpdateUserRequest.builder().password("newpassword").name("수정된 이름").build();

		AppUser user = AppUser.builder().id(userId).email("test@example.com").password("oldpassword").name("이전 이름")
				.build();

		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

		// when
		UserResponse response = userService.updateUser(userId, request);

		// then
		assertThat(response.getName()).isEqualTo("수정된 이름");

		then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(userId);
	}

	@Test
	@DisplayName("회원 정보 수정 실패 - 존재하지 않는 회원")
	void updateUser_NotFound() {
		// given
		Long userId = 999L;
		UpdateUserRequest request = UpdateUserRequest.builder().name("수정된 이름").build();

		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.updateUser(userId, request)).isInstanceOf(ResourceNotFoundException.class);

		then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(userId);
	}

	@Test
	@DisplayName("회원 삭제 성공 (Soft Delete)")
	void deleteUser_Success() {
		// given
		Long userId = 1L;
		AppUser user = AppUser.builder().id(userId).email("test@example.com").password("password123").name("테스트 사용자")
				.build();

		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

		// when
		userService.deleteUser(userId);

		// then
		assertThat(user.getDeletedAt()).isNotNull();
		assertThat(user.isDeleted()).isTrue();

		then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(userId);
	}

	@Test
	@DisplayName("회원 삭제 실패 - 존재하지 않는 회원")
	void deleteUser_NotFound() {
		// given
		Long userId = 999L;
		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.deleteUser(userId)).isInstanceOf(ResourceNotFoundException.class);

		then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(userId);
	}
}
