package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.*;
import com.ecommerce.userservice.dto.response.LoginResponse;
import com.ecommerce.userservice.dto.response.TokenResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.enums.Gender;
import com.ecommerce.common.enums.UserRole;
import com.ecommerce.userservice.exception.UserDomainException;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.common.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    TokenService tokenService;

    @InjectMocks
    UserService userService;

    private User testUser;
    private static final Long USER_ID = 1L;
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "Password123!";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";

    @BeforeEach
    void setUp() {
        testUser = createTestUser(USER_ID, EMAIL, ENCODED_PASSWORD, true);
    }

    @Nested
    @DisplayName("???    ????? ??)
    class SignUpTest {

        @Test
        @DisplayName("???    ???   ")
        void signUp_success() {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email(EMAIL)
                    .password(PASSWORD)
                    .name("??? ?  ???")
                    .phoneNumber("010-1234-5678")
                    .gender(Gender.MALE)
                    .build();

            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                ReflectionTestUtils.setField(user, "id", USER_ID);
                return user;
            });

            // when
            userService.signUp(request);

            // then
            verify(userRepository).existsByEmail(EMAIL);
            verify(passwordEncoder).encode(PASSWORD);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("???    ????   -    ????  ??)
        void signUp_duplicateEmail_throwsException() {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email(EMAIL)
                    .password(PASSWORD)
                    .name("??? ?  ???")
                    .phoneNumber("010-1234-5678")
                    .gender(Gender.MALE)
                    .build();

            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("?? ? ????   ????  ??  ??  ");

            verify(userRepository).existsByEmail(EMAIL);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("   ?????? ??)
    class LoginTest {

        @Test
        @DisplayName("   ????   ")
        void login_success() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email(EMAIL)
                    .password(PASSWORD)
                    .build();

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(jwtTokenProvider.createAccessToken(USER_ID, EMAIL, UserRole.USER)).thenReturn(ACCESS_TOKEN);
            when(jwtTokenProvider.createRefreshToken(USER_ID)).thenReturn(REFRESH_TOKEN);

            // when
            LoginResponse response = userService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(USER_ID);
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
            verify(tokenService).saveRefreshToken(USER_ID, REFRESH_TOKEN);
        }

        @Test
        @DisplayName("   ?????   -    ???? ??   ??  ??)
        void login_emailNotFound_throwsException() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("notexist@example.com")
                    .password(PASSWORD)
                    .build();

            when(userRepository.findByEmail("notexist@example.com")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("??  ??   ??  ??   ??  ??  ");
        }

        @Test
        @DisplayName("   ?????   - ?? ?   ???  ?  ?)
        void login_invalidPassword_throwsException() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email(EMAIL)
                    .password("wrongPassword")
                    .build();

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", ENCODED_PASSWORD)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("?    ?? ?   ?  ? ??  ??? ??  ??  ");
        }

        @Test
        @DisplayName("   ?????   - ??  ????? ")
        void login_withdrawnUser_throwsException() {
            // given
            User withdrawnUser = createTestUser(USER_ID, EMAIL, ENCODED_PASSWORD, false);
            LoginRequest request = LoginRequest.builder()
                    .email(EMAIL)
                    .password(PASSWORD)
                    .build();

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(withdrawnUser));

            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("?? ? ??  ????? ??  ??);
        }
    }

    @Nested
    @DisplayName("   ??    ??? ??)
    class LogoutTest {

        @Test
        @DisplayName("   ??    ?   ")
        void logout_success() {
            // given
            long expiration = 3600000L;
            when(jwtTokenProvider.getExpiration(ACCESS_TOKEN)).thenReturn(expiration);

            // when
            userService.logout(USER_ID, ACCESS_TOKEN);

            // then
            verify(jwtTokenProvider).getExpiration(ACCESS_TOKEN);
            verify(tokenService).addToBlacklist(ACCESS_TOKEN, expiration);
            verify(tokenService).deleteRefreshToken(USER_ID);
        }
    }

    @Nested
    @DisplayName("?       ????? ??)
    class RefreshTokenTest {

        @Test
        @DisplayName("?       ???   ")
        void refreshToken_success() {
            // given
            String newAccessToken = "newAccessToken";
            String newRefreshToken = "newRefreshToken";

            when(jwtTokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtTokenProvider.isRefreshToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtTokenProvider.getUserId(REFRESH_TOKEN)).thenReturn(USER_ID);
            when(tokenService.validateRefreshToken(USER_ID, REFRESH_TOKEN)).thenReturn(true);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(jwtTokenProvider.createAccessToken(USER_ID, EMAIL, UserRole.USER)).thenReturn(newAccessToken);
            when(jwtTokenProvider.createRefreshToken(USER_ID)).thenReturn(newRefreshToken);

            // when
            TokenResponse response = userService.refreshToken(REFRESH_TOKEN);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
            assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);
            verify(tokenService).saveRefreshToken(USER_ID, newRefreshToken);
        }

        @Test
        @DisplayName("?       ????   - ?   ??? ??? ?   ")
        void refreshToken_invalidToken_throwsException() {
            // given
            when(jwtTokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("?   ??? ??? ?   ??  ??);
        }

        @Test
        @DisplayName("?       ????   - ???  ??   ???  ?  ?)
        void refreshToken_mismatch_throwsException() {
            // given
            when(jwtTokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtTokenProvider.isRefreshToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtTokenProvider.getUserId(REFRESH_TOKEN)).thenReturn(USER_ID);
            when(tokenService.validateRefreshToken(USER_ID, REFRESH_TOKEN)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("Refresh Token????  ??? ??  ??  ");
        }

        @Test
        @DisplayName("?       ????   - ??  ????? ")
        void refreshToken_withdrawnUser_throwsException() {
            // given
            User withdrawnUser = createTestUser(USER_ID, EMAIL, ENCODED_PASSWORD, false);

            when(jwtTokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtTokenProvider.isRefreshToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtTokenProvider.getUserId(REFRESH_TOKEN)).thenReturn(USER_ID);
            when(tokenService.validateRefreshToken(USER_ID, REFRESH_TOKEN)).thenReturn(true);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(withdrawnUser));

            // when & then
            assertThatThrownBy(() -> userService.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("?? ? ??  ????? ??  ??);
        }
    }

    @Nested
    @DisplayName("?   ????   ??? ??)
    class UpdateProfileTest {

        @Test
        @DisplayName("?   ????   ?   ")
        void updateProfile_success() {
            // given
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .name("??  ??)
                    .phoneNumber("010-9999-9999")
                    .gender(Gender.FEMALE)
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // when
            UserResponse response = userService.updateProfile(USER_ID, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("??  ??);
            assertThat(response.getPhoneNumber()).isEqualTo("010-9999-9999");
        }

        @Test
        @DisplayName("?   ????   ??   - ???????  ")
        void updateProfile_userNotFound_throwsException() {
            // given
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .name("??  ??)
                    .phoneNumber("010-9999-9999")
                    .gender(Gender.FEMALE)
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateProfile(USER_ID, request))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("???? ?    ??????  ??  ");
        }
    }

    @Nested
    @DisplayName("?? ?   ??    ???? ??)
    class ChangePasswordTest {

        @Test
        @DisplayName("?? ?   ??    ??   ")
        void changePassword_success() {
            // given
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword(PASSWORD)
                    .newPassword("NewPassword123!")
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(passwordEncoder.matches("NewPassword123!", ENCODED_PASSWORD)).thenReturn(false);
            when(passwordEncoder.encode("NewPassword123!")).thenReturn("newEncodedPassword");

            // when
            userService.changePassword(USER_ID, request);

            // then
            verify(passwordEncoder).encode("NewPassword123!");
        }

        @Test
        @DisplayName("?? ?   ??    ???   - ?    ?? ?   ???  ?  ?)
        void changePassword_wrongCurrentPassword_throwsException() {
            // given
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("wrongPassword")
                    .newPassword("NewPassword123!")
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", ENCODED_PASSWORD)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.changePassword(USER_ID, request))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("?    ?? ?   ?  ? ??  ??? ??  ??  ");
        }

        @Test
        @DisplayName("?? ?   ??    ???   - ???? ?   ?  ?    ?  ???  ")
        void changePassword_samePassword_throwsException() {
            // given
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword(PASSWORD)
                    .newPassword(PASSWORD)
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.changePassword(USER_ID, request))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("???? ?   ????    ?? ?   ??? ???????  ??);
        }
    }

    @Nested
    @DisplayName("???  ??   ??? ??)
    class WithdrawTest {

        @Test
        @DisplayName("???  ??   ?   ")
        void withdraw_success() {
            // given
            WithdrawRequest request = WithdrawRequest.builder()
                    .password(PASSWORD)
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

            // when
            userService.withdraw(USER_ID, request);

            // then
            assertThat(testUser.getIsActive()).isFalse();
            verify(tokenService).deleteRefreshToken(USER_ID);
        }

        @Test
        @DisplayName("???  ??   ??   - ?? ? ??  ????? ")
        void withdraw_alreadyWithdrawn_throwsException() {
            // given
            User withdrawnUser = createTestUser(USER_ID, EMAIL, ENCODED_PASSWORD, false);
            WithdrawRequest request = WithdrawRequest.builder()
                    .password(PASSWORD)
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(withdrawnUser));

            // when & then
            assertThatThrownBy(() -> userService.withdraw(USER_ID, request))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("?? ? ??  ????? ??  ??);
        }

        @Test
        @DisplayName("???  ??   ??   - ?? ?   ???  ?  ?)
        void withdraw_wrongPassword_throwsException() {
            // given
            WithdrawRequest request = WithdrawRequest.builder()
                    .password("wrongPassword")
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", ENCODED_PASSWORD)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.withdraw(USER_ID, request))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("?    ?? ?   ?  ? ??  ??? ??  ??  ");
        }
    }

    @Nested
    @DisplayName("???       ????? ??)
    class GetMyProfileTest {

        @Test
        @DisplayName("???       ???   ")
        void getMyProfile_success() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // when
            UserResponse response = userService.getMyProfile(USER_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(USER_ID);
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            assertThat(response.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("???       ????   - ???????  ")
        void getMyProfile_userNotFound_throwsException() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getMyProfile(USER_ID))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("???? ?    ??????  ??  ");
        }
    }

    @Nested
    @DisplayName("??  ??   ?????   ????? ??)
    class FindUserByEmailTest {

        @Test
        @DisplayName("??  ??   ?????   ???   ")
        void findUserByEmail_success() {
            // given
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));

            // when
            UserResponse response = userService.findUserByEmail(EMAIL);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("??  ??   ?????   ????   -    ???? ??   ??  ??)
        void findUserByEmail_notFound_throwsException() {
            // given
            when(userRepository.findByEmail("notexist@example.com")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.findUserByEmail("notexist@example.com"))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("??  ??   ??  ??   ??  ??  ");
        }
    }

    private User createTestUser(Long id, String email, String password, boolean isActive) {
        User user = User.create(email, password, "??? ?  ???", "010-1234-5678", Gender.MALE);
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "isActive", isActive);
        return user;
    }
}
