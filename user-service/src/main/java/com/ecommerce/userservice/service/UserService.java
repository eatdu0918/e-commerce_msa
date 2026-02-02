package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.*;
import com.ecommerce.userservice.dto.response.LoginResponse;
import com.ecommerce.userservice.dto.response.TokenResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.exception.UserDomainException;
import com.ecommerce.userservice.exception.UserDomainExceptionCode;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

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

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserDomainException(UserDomainExceptionCode.EmailNotFoundException));

        // 탈퇴 회원 확인
        if (!user.getIsActive()) {
            throw new UserDomainException(UserDomainExceptionCode.UserAlreadyWithdrawnException);
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UserDomainException(UserDomainExceptionCode.InvalidPasswordException);
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // Redis에 Refresh Token 저장
        tokenService.saveRefreshToken(user.getId(), refreshToken);

        log.info("로그인 성공: userId={}, email={}", user.getId(), user.getEmail());

        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 로그아웃
     */
    public void logout(Long userId, String accessToken) {
        // Access Token Blacklist에 추가
        long expiration = jwtTokenProvider.getExpiration(accessToken);
        tokenService.addToBlacklist(accessToken, expiration);

        // Redis에서 Refresh Token 삭제
        tokenService.deleteRefreshToken(userId);

        log.info("로그아웃 완료: userId={}", userId);
    }

    /**
     * 토큰 갱신
     */
    @Transactional(readOnly = true)
    public TokenResponse refreshToken(String refreshToken) {
        // Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UserDomainException(UserDomainExceptionCode.InvalidTokenException);
        }

        // Refresh Token 타입 확인
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new UserDomainException(UserDomainExceptionCode.InvalidTokenException);
        }

        // 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // Redis에 저장된 Refresh Token과 비교
        if (!tokenService.validateRefreshToken(userId, refreshToken)) {
            throw new UserDomainException(UserDomainExceptionCode.RefreshTokenMismatchException);
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserDomainException(UserDomainExceptionCode.UserNotFoundException));

        // 탈퇴 회원 확인
        if (!user.getIsActive()) {
            throw new UserDomainException(UserDomainExceptionCode.UserAlreadyWithdrawnException);
        }

        // 새 Access Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        // 새 Refresh Token 생성 (Refresh Token Rotation)
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        tokenService.saveRefreshToken(userId, newRefreshToken);

        log.info("토큰 갱신 완료: userId={}", userId);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
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

        // Redis에서 Refresh Token 삭제
        tokenService.deleteRefreshToken(userId);

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
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
