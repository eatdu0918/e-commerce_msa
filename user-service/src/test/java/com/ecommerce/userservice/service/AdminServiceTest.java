package com.ecommerce.userservice.service;

import com.ecommerce.common.response.PageResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.enums.Gender;
import com.ecommerce.common.enums.UserRole;
import com.ecommerce.userservice.exception.UserDomainException;
import com.ecommerce.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    TokenService tokenService;

    @InjectMocks
    AdminService adminService;

    private User testUser;
    private static final Long USER_ID = 1L;
    private static final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = createTestUser(USER_ID, EMAIL, true, UserRole.USER);
    }

    @Nested
    @DisplayName("?    ???     ????? ??)
    class GetAllUsersTest {

        @Test
        @DisplayName("?    ???     ???   ")
        void getAllUsers_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            User user1 = createTestUser(1L, "user1@example.com", true, UserRole.USER);
            User user2 = createTestUser(2L, "user2@example.com", true, UserRole.ADMIN);
            Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

            when(userRepository.findAll(pageable)).thenReturn(userPage);

            // when
            PageResponse<UserResponse> response = adminService.getAllUsers(pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getTotalElements()).isEqualTo(2);
            verify(userRepository).findAll(pageable);
        }

        @Test
        @DisplayName("?????      ?   ??)
        void getAllUsers_empty() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(userRepository.findAll(pageable)).thenReturn(emptyPage);

            // when
            PageResponse<UserResponse> response = adminService.getAllUsers(pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("???  ?       ????? ??)
    class GetUserByIdTest {

        @Test
        @DisplayName("???  ?       ???   ")
        void getUserById_success() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // when
            UserResponse response = adminService.getUserById(USER_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(USER_ID);
            assertThat(response.getEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("???  ?       ????   -    ???? ??   ??? ")
        void getUserById_notFound_throwsException() {
            // given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminService.getUserById(999L))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("???? ?    ??????  ??  ");
        }
    }

    @Nested
    @DisplayName("???     ????   ??? ??)
    class DeleteUserTest {

        @Test
        @DisplayName("???     ????   ?   ")
        void deleteUser_success() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // when
            adminService.deleteUser(USER_ID);

            // then
            assertThat(testUser.getIsActive()).isFalse();
            verify(tokenService).deleteRefreshToken(USER_ID);
        }

        @Test
        @DisplayName("???     ????   ??   -    ???? ??   ??? ")
        void deleteUser_notFound_throwsException() {
            // given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminService.deleteUser(999L))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("???? ?    ??????  ??  ");
        }

        @Test
        @DisplayName("???     ????   ??   - ?? ? ??  ????? ")
        void deleteUser_alreadyWithdrawn_throwsException() {
            // given
            User withdrawnUser = createTestUser(USER_ID, EMAIL, false, UserRole.USER);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(withdrawnUser));

            // when & then
            assertThatThrownBy(() -> adminService.deleteUser(USER_ID))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("?? ? ??  ????? ??  ??);
        }
    }

    @Nested
    @DisplayName("???      ?    ???? ??)
    class ChangeUserRoleTest {

        @Test
        @DisplayName("???      ?    ??    - USER to ADMIN")
        void changeUserRole_toAdmin_success() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // when
            UserResponse response = adminService.changeUserRole(USER_ID, "ADMIN");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getRole()).isEqualTo("ADMIN");
            verify(tokenService).deleteRefreshToken(USER_ID);
        }

        @Test
        @DisplayName("???      ?    ??    - ADMIN to USER")
        void changeUserRole_toUser_success() {
            // given
            User adminUser = createTestUser(USER_ID, EMAIL, true, UserRole.ADMIN);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(adminUser));

            // when
            UserResponse response = adminService.changeUserRole(USER_ID, "USER");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getRole()).isEqualTo("USER");
        }

        @Test
        @DisplayName("???      ?    ??    - ???????   ?)
        void changeUserRole_lowercaseRole_success() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // when
            UserResponse response = adminService.changeUserRole(USER_ID, "admin");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getRole()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("???      ?    ???   -    ???? ??   ??? ")
        void changeUserRole_userNotFound_throwsException() {
            // given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminService.changeUserRole(999L, "ADMIN"))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("???? ?    ??????  ??  ");
        }

        @Test
        @DisplayName("???      ?    ???   - ??  ????? ")
        void changeUserRole_withdrawnUser_throwsException() {
            // given
            User withdrawnUser = createTestUser(USER_ID, EMAIL, false, UserRole.USER);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(withdrawnUser));

            // when & then
            assertThatThrownBy(() -> adminService.changeUserRole(USER_ID, "ADMIN"))
                    .isInstanceOf(UserDomainException.class)
                    .hasMessageContaining("?? ? ??  ????? ??  ??);
        }

        @Test
        @DisplayName("???      ?    ???   - ?   ??? ??? ??   ?)
        void changeUserRole_invalidRole_throwsException() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> adminService.changeUserRole(USER_ID, "INVALID_ROLE"))
                    .isInstanceOf(UserDomainException.class);
        }
    }

    private User createTestUser(Long id, String email, boolean isActive, UserRole role) {
        User user = User.create(email, "encodedPassword", "??? ?  ???", "010-1234-5678", Gender.MALE);
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "isActive", isActive);
        ReflectionTestUtils.setField(user, "role", role);
        return user;
    }
}
