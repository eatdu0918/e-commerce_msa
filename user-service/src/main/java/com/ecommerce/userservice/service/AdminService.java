package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.response.PageResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.enums.UserRole;
import com.ecommerce.userservice.exception.UserDomainException;
import com.ecommerce.userservice.exception.UserDomainExceptionCode;
import com.ecommerce.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        return PageResponse.from(
                userRepository.findAll(pageable).map(this::convertToUserResponse)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserDomainException(UserDomainExceptionCode.UserNotFoundException));
        return convertToUserResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserDomainException(UserDomainExceptionCode.UserNotFoundException));

        if (!user.getIsActive()) {
            throw new UserDomainException(UserDomainExceptionCode.UserAlreadyWithdrawnException);
        }

        user.withdraw();

        // Redis에서 Refresh Token 삭제
        tokenService.deleteRefreshToken(userId);

        log.info("관리자에 의한 회원 탈퇴 처리: userId={}", userId);
    }

    @Transactional
    public UserResponse changeUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserDomainException(UserDomainExceptionCode.UserNotFoundException));

        if (!user.getIsActive()) {
            throw new UserDomainException(UserDomainExceptionCode.UserAlreadyWithdrawnException);
        }

        UserRole newRole;
        try {
            String normalized = roleName.trim().toUpperCase();
            if (normalized.startsWith("ROLE_")) {
                normalized = normalized.substring("ROLE_".length());
            }
            newRole = UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new UserDomainException(UserDomainExceptionCode.InvalidTokenException);
        }

        user.changeRole(newRole);

        // 권한 변경 시 기존 토큰 무효화를 위해 Refresh Token 삭제
        tokenService.deleteRefreshToken(userId);

        log.info("회원 권한 변경: userId={}, newRole={}", userId, newRole);
        return convertToUserResponse(user);
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
