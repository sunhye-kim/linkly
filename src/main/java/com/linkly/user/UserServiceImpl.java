package com.linkly.user;

import com.linkly.domain.AppUser;
import com.linkly.global.exception.ResourceNotFoundException;
import com.linkly.user.dto.UpdateUserRequest;
import com.linkly.user.dto.UserResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

	private final AppUserRepository userRepository;

	@Override
	public UserResponse getUserById(Long userId) {
		log.debug("회원 조회: userId={}", userId);

		AppUser user = userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", userId));

		return UserResponse.from(user);
	}

	@Override
	public UserResponse getUserByEmail(String email) {
		log.debug("이메일로 회원 조회: email={}", email);

		AppUser user = userRepository.findByEmailAndDeletedAtIsNull(email)
				.orElseThrow(() -> new ResourceNotFoundException("User", "email=" + email));

		return UserResponse.from(user);
	}

	@Override
	public List<UserResponse> getAllUsers() {
		log.debug("전체 회원 조회");

		return userRepository.findAll().stream().filter(user -> user.getDeletedAt() == null).map(UserResponse::from)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public UserResponse updateUser(Long userId, UpdateUserRequest request) {
		log.info("회원 정보 수정: userId={}", userId);

		AppUser user = userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", userId));

		// TODO: 비밀번호 수정 시 암호화 필요
		user.updateInfo(request.getPassword(), request.getName());

		log.info("회원 정보 수정 완료: userId={}", userId);

		return UserResponse.from(user);
	}

	@Override
	@Transactional
	public void deleteUser(Long userId) {
		log.info("회원 삭제 (Soft Delete): userId={}", userId);

		AppUser user = userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", userId));

		user.softDelete();

		log.info("회원 삭제 완료: userId={}", userId);
	}
}
