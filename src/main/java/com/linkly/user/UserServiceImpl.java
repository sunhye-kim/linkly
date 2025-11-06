package com.linkly.user;

import com.linkly.domain.AppUser;
import com.linkly.global.exception.InvalidRequestException;
import com.linkly.global.exception.ResourceNotFoundException;
import com.linkly.user.dto.CreateUserRequest;
import com.linkly.user.dto.UpdateUserRequest;
import com.linkly.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final AppUserRepository userRepository;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("회원 가입 시도: email={}", request.getEmail());

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidRequestException(
                    "이미 사용 중인 이메일입니다",
                    "email=" + request.getEmail()
            );
        }

        // TODO: 비밀번호 암호화 (BCryptPasswordEncoder 사용)
        // 현재는 평문으로 저장 - Spring Security 도입 시 개선 필요
        AppUser user = AppUser.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .build();

        AppUser savedUser = userRepository.save(user);
        log.info("회원 가입 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        return UserResponse.from(savedUser);
    }

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

        return userRepository.findAll().stream()
                .filter(user -> user.getDeletedAt() == null)
                .map(UserResponse::from)
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