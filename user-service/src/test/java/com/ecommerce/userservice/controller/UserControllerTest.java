package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.request.*;
import com.ecommerce.userservice.dto.response.LoginResponse;
import com.ecommerce.userservice.dto.response.TokenResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.enums.Gender;
import com.ecommerce.userservice.exception.UserDomainException;
import com.ecommerce.userservice.exception.UserDomainExceptionCode;
import com.ecommerce.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/auth/signup - 회원가입")
    class SignUpTest {

        @Test
        @DisplayName("회원가입 성공")
        void signUp_success() throws Exception {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email("test@example.com")
                    .password("Password123!")
                    .name("테스트유저")
                    .phoneNumber("010-1234-5678")
                    .gender(Gender.MALE)
                    .build();

            doNothing().when(userService).signUp(any(SignUpRequest.class));

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("회원가입 실패 - 이메일 형식 오류")
        void signUp_invalidEmail_fail() throws Exception {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email("invalid-email")
                    .password("Password123!")
                    .name("테스트유저")
                    .phoneNumber("010-1234-5678")
                    .gender(Gender.MALE)
                    .build();

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("회원가입 실패 - 비밀번호 형식 오류")
        void signUp_invalidPassword_fail() throws Exception {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email("test@example.com")
                    .password("weak")
                    .name("테스트유저")
                    .phoneNumber("010-1234-5678")
                    .gender(Gender.MALE)
                    .build();

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("회원가입 실패 - 중복 이메일")
        void signUp_duplicateEmail_fail() throws Exception {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email("test@example.com")
                    .password("Password123!")
                    .name("테스트유저")
                    .phoneNumber("010-1234-5678")
                    .gender(Gender.MALE)
                    .build();

            doThrow(new UserDomainException(UserDomainExceptionCode.DuplicateEmailException))
                    .when(userService).signUp(any(SignUpRequest.class));

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAlreadyReported());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login - 로그인")
    class LoginTest {

        @Test
        @DisplayName("로그인 성공")
        void login_success() throws Exception {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("Password123!")
                    .build();

            LoginResponse response = LoginResponse.builder()
                    .userId(1L)
                    .email("test@example.com")
                    .role("USER")
                    .accessToken("accessToken")
                    .refreshToken("refreshToken")
                    .build();

            when(userService.login(any(LoginRequest.class))).thenReturn(response);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(1))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.accessToken").value("accessToken"));
        }

        @Test
        @DisplayName("로그인 실패 - 존재하지 않는 이메일")
        void login_emailNotFound_fail() throws Exception {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("notexist@example.com")
                    .password("Password123!")
                    .build();

            when(userService.login(any(LoginRequest.class)))
                    .thenThrow(new UserDomainException(UserDomainExceptionCode.EmailNotFoundException));

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("로그인 실패 - 비밀번호 불일치")
        void login_invalidPassword_fail() throws Exception {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("wrongPassword!")
                    .build();

            when(userService.login(any(LoginRequest.class)))
                    .thenThrow(new UserDomainException(UserDomainExceptionCode.InvalidPasswordException));

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh - 토큰 갱신")
    class RefreshTokenTest {

        @Test
        @DisplayName("토큰 갱신 성공")
        void refreshToken_success() throws Exception {
            // given
            String requestBody = "{\"refreshToken\":\"validRefreshToken\"}";

            TokenResponse response = TokenResponse.builder()
                    .accessToken("newAccessToken")
                    .refreshToken("newRefreshToken")
                    .build();

            when(userService.refreshToken(anyString())).thenReturn(response);

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"))
                    .andExpect(jsonPath("$.data.refreshToken").value("newRefreshToken"));
        }

        @Test
        @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰")
        void refreshToken_invalidToken_fail() throws Exception {
            // given
            String requestBody = "{\"refreshToken\":\"invalidToken\"}";

            when(userService.refreshToken(anyString()))
                    .thenThrow(new UserDomainException(UserDomainExceptionCode.InvalidTokenException));

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/findUser/{email} - 이메일로 사용자 조회")
    class FindUserByEmailTest {

        @Test
        @DisplayName("이메일로 사용자 조회 성공")
        void findUserByEmail_success() throws Exception {
            // given
            UserResponse response = UserResponse.builder()
                    .id(1L)
                    .email("test@example.com")
                    .name("테스트유저")
                    .phoneNumber("010-1234-5678")
                    .role("USER")
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userService.findUserByEmail("test@example.com")).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/auth/findUser/test@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }

        @Test
        @DisplayName("이메일로 사용자 조회 실패 - 존재하지 않음")
        void findUserByEmail_notFound_fail() throws Exception {
            // given
            when(userService.findUserByEmail("notexist@example.com"))
                    .thenThrow(new UserDomainException(UserDomainExceptionCode.EmailNotFoundException));

            // when & then
            mockMvc.perform(get("/api/auth/findUser/notexist@example.com"))
                    .andExpect(status().isNotFound());
        }
    }
}
