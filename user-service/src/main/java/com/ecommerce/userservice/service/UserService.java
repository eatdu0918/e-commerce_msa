package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.*;
import com.ecommerce.userservice.dto.response.LoginResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.exception.UserDomainException;
import com.ecommerce.userservice.exception.UserDomainExceptionCode;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @Transactional
    public void signUp(SignUpRequest request) {
        log.info("회원가입 시도: email={}", request.getEmail());

        validateDuplicateEmail(request.getEmail());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.create(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getPhoneNumber(),
                request.getGender()
        );

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: userId={}, email={}", savedUser.getId(), request.getEmail());
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserDomainException(UserDomainExceptionCode.DuplicateEmailException);
        }
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return LoginResponse.builder()
                .userId(userDetails.getUserId())
                .email(userDetails.getEmail())
                .build();
    }

    public UserResponse findUserByEmail(String email) {
        User user = getUserByEmail(email);
        return convertToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getActiveUserById(userId);
        user.updateProfile(request.getName(), request.getPhoneNumber(), request.getGender());
        log.info("프로필 수정 완료: userId={}", userId);
        return convertToUserResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getActiveUserById(userId);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UserDomainException(UserDomainExceptionCode.InvalidPasswordException);
        }

        // 새 비밀번호가 기존과 동일한지 확인
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new UserDomainException(UserDomainExceptionCode.SamePasswordException);
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.changePassword(encodedNewPassword);
        log.info("비밀번호 변경 완료: userId={}", userId);
    }

    @Transactional
    public void withdraw(Long userId, WithdrawRequest request) {
        User user = getUserById(userId);

        // 이미 탈퇴한 회원인지 확인
        if (!user.getIsActive()) {
            throw new UserDomainException(UserDomainExceptionCode.UserAlreadyWithdrawnException);
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserDomainException(UserDomainExceptionCode.InvalidPasswordException);
        }

        user.withdraw();
        log.info("회원 탈퇴 완료: userId={}", userId);
    }

    public UserResponse getMyProfile(Long userId) {
        User user = getActiveUserById(userId);
        return convertToUserResponse(user);
    }

    private User getActiveUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserDomainException(UserDomainExceptionCode.UserNotFoundException));
        validateActiveUser(user);
        return user;
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserDomainException(UserDomainExceptionCode.UserNotFoundException));
    }

    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserDomainException(UserDomainExceptionCode.EmailNotFoundException));
        validateActiveUser(user);
        return user;
    }

    private void validateActiveUser(User user) {
        if (!user.getIsActive()) {
            throw new UserDomainException(UserDomainExceptionCode.UserAlreadyWithdrawnException);
        }
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
