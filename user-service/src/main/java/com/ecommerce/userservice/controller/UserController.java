package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.request.*;
import com.ecommerce.userservice.dto.response.LoginResponse;
import com.ecommerce.userservice.dto.response.TokenResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.response.ApiResponse;
import com.ecommerce.userservice.security.CustomUserDetails;
import com.ecommerce.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @PostMapping("/signup")
    public ApiResponse<Void> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("POST /api/auth/signup - 회원가입 요청");
        userService.signUp(request);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("POST /api/auth/login - 로그인");
        LoginResponse loginResponse = userService.login(loginRequest);
        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        log.info("POST /api/auth/logout - 로그아웃: userId={}", userDetails.getUserId());
        String accessToken = extractToken(request);
        userService.logout(userDetails.getUserId(), accessToken);
        return ApiResponse.ok();
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("POST /api/auth/refresh - 토큰 갱신");
        TokenResponse tokenResponse = userService.refreshToken(request.getRefreshToken());
        return ApiResponse.success(tokenResponse);
    }

    @GetMapping("/findUser/{email}")
    public ApiResponse<UserResponse> findUser(@PathVariable("email") String email) {
        log.info("GET /api/auth/findUser - 회원정보조회");
        return ApiResponse.success(userService.findUserByEmail(email));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/auth/me - 내 정보 조회: userId={}", userDetails.getUserId());
        return ApiResponse.success(userService.getMyProfile(userDetails.getUserId()));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("PUT /api/auth/profile - 프로필 수정: userId={}", userDetails.getUserId());
        return ApiResponse.success(userService.updateProfile(userDetails.getUserId(), request));
    }

    @PutMapping("/changePassword")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("PUT /api/auth/change-password - 비밀번호 변경: userId={}", userDetails.getUserId());
        userService.changePassword(userDetails.getUserId(), request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WithdrawRequest request) {
        log.info("DELETE /api/auth/withdraw - 회원 탈퇴: userId={}", userDetails.getUserId());
        userService.withdraw(userDetails.getUserId(), request);
        return ApiResponse.ok();
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
