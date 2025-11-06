package com.linkly.user;

import com.linkly.user.dto.CreateUserRequest;
import com.linkly.user.dto.UpdateUserRequest;
import com.linkly.user.dto.UserResponse;

import java.util.List;

/**
 * 회원 관리 서비스 인터페이스
 */
public interface UserService {

    /**
     * 회원 가입
     *
     * @param request 회원 가입 정보
     * @return 생성된 회원 정보
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * 회원 ID로 조회
     *
     * @param userId 회원 ID
     * @return 회원 정보
     */
    UserResponse getUserById(Long userId);

    /**
     * 이메일로 회원 조회
     *
     * @param email 이메일
     * @return 회원 정보
     */
    UserResponse getUserByEmail(String email);

    /**
     * 전체 회원 조회 (활성 회원만)
     *
     * @return 회원 목록
     */
    List<UserResponse> getAllUsers();

    /**
     * 회원 정보 수정
     *
     * @param userId  회원 ID
     * @param request 수정할 정보
     * @return 수정된 회원 정보
     */
    UserResponse updateUser(Long userId, UpdateUserRequest request);

    /**
     * 회원 삭제 (Soft Delete)
     *
     * @param userId 회원 ID
     */
    void deleteUser(Long userId);
}