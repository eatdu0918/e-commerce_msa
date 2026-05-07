package com.ecommerce.userservice.service;

import com.ecommerce.common.security.JwtTokenProvider;
import com.ecommerce.common.service.TokenService;
import com.ecommerce.userservice.dto.request.*;
import com.ecommerce.userservice.dto.response.LoginResponse;
import com.ecommerce.userservice.dto.response.TokenResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.exception.UserDomainException;
import com.ecommerce.userservice.exception.UserDomainExceptionCode;
import com.ecommerce.userservice.repository.UserRepository;
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
        log.info("???    ????  : email={}", request.getEmail());

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
        log.info("???    ???   : userId={}, email={}", savedUser.getId(), request.getEmail());
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserDomainException(UserDomainExceptionCode.DuplicateEmailException);
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        // ??  ??   ?????   ??
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserDomainException(UserDomainExceptionCode.EmailNotFoundException));

        // ??   ???  ?   
        if (!user.getIsActive()) {
            throw new UserDomainException(UserDomainExceptionCode.UserAlreadyWithdrawnException);
        }

        // ?? ?   ???   
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UserDomainException(UserDomainExceptionCode.InvalidPasswordException);
        }

        // JWT ?    ??  
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // Redis??Refresh Token ????
        tokenService.saveRefreshToken(user.getId(), refreshToken);

        log.info("   ????   : userId={}, email={}", user.getId(), user.getEmail());

        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     *    ??   
     */
    public void logout(Long userId, String accessToken) {
        // Access Token Blacklist???  ?
        long expiration = jwtTokenProvider.getExpiration(accessToken);
        tokenService.addToBlacklist(accessToken, expiration);

        // Redis? ?  Refresh Token ????
        tokenService.deleteRefreshToken(userId);

        log.info("   ??    ?   : userId={}", userId);
    }

    /**
     * ?       ??
     */
    @Transactional(readOnly = true)
    public TokenResponse refreshToken(String refreshToken) {
        // Refresh Token ?   ??    ?
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UserDomainException(UserDomainExceptionCode.InvalidTokenException);
        }

        // Refresh Token ?????   
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new UserDomainException(UserDomainExceptionCode.InvalidTokenException);
        }

        // ?????ID ?  ??
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // Redis?????  ?Refresh Token???? ??
        if (!tokenService.validateRefreshToken(userId, refreshToken)) {
            throw new UserDomainException(UserDomainExceptionCode.RefreshTokenMismatchException);
        }

        // ?????   ??
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserDomainException(UserDomainExceptionCode.UserNotFoundException));

        // ??   ???  ?   
        if (!user.getIsActive()) {
            throw new UserDomainException(UserDomainExceptionCode.UserAlreadyWithdrawnException);
        }

        // ??Access Token ??  
        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        // ??Refresh Token ??   (Refresh Token Rotation)
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        tokenService.saveRefreshToken(userId, newRefreshToken);

        log.info("?       ???   : userId={}", userId);

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
        log.info("?   ????   ?   : userId={}", userId);
        return convertToUserResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getActiveUserById(userId);

        // ?    ?? ?   ???   
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UserDomainException(UserDomainExceptionCode.InvalidPasswordException);
        }

        // ???? ?   ?  ?    ?  ???  ??? ?   
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new UserDomainException(UserDomainExceptionCode.SamePasswordException);
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.changePassword(encodedNewPassword);
        log.info("?? ?   ??    ??   : userId={}", userId);
    }

    @Transactional
    public void withdraw(Long userId, WithdrawRequest request) {
        User user = getUserById(userId);

        // ?? ? ??  ????? ? ? ?   
        if (!user.getIsActive()) {
            throw new UserDomainException(UserDomainExceptionCode.UserAlreadyWithdrawnException);
        }

        // ?? ?   ???   
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserDomainException(UserDomainExceptionCode.InvalidPasswordException);
        }

        user.withdraw();

        // Redis? ?  Refresh Token ????
        tokenService.deleteRefreshToken(userId);

        log.info("???  ??   ?   : userId={}", userId);
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
