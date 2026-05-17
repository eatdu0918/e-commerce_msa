package com.ecommerce.userservice.controller;

import com.ecommerce.common.response.PageResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.exception.UserDomainException;
import com.ecommerce.userservice.exception.UserDomainExceptionCode;
import com.ecommerce.common.security.JwtTokenProvider;
import com.ecommerce.userservice.service.AdminService;
import com.ecommerce.common.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

        @Autowired
        MockMvc mockMvc;

        @MockBean
        AdminService adminService;

        @MockBean
        JwtTokenProvider jwtTokenProvider;

        @MockBean
        TokenService tokenService;

        @Nested
        @DisplayName("GET /api/admin/users - 전체 회원 조회")
        class GetAllUsersTest {

                @Test
                @DisplayName("GET /api/admin/users - 전체 회원 조회")
                void getAllUsers_success() throws Exception {
                        // given
                        UserResponse user1 = createUserResponse(1L, "user1@example.com", "USER");
                        UserResponse user2 = createUserResponse(2L, "user2@example.com", "ADMIN");

                        PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                                        .content(List.of(user1, user2))
                                        .pageNumber(0)
                                        .pageSize(10)
                                        .totalElements(2L)
                                        .totalPages(1)
                                        .first(true)
                                        .last(true)
                                        .build();

                        when(adminService.getAllUsers(any(Pageable.class))).thenReturn(pageResponse);

                        // when & then
                        mockMvc.perform(get("/api/admin/users"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.content").isArray())
                                        .andExpect(jsonPath("$.data.content.length()").value(2))
                                        .andExpect(jsonPath("$.data.totalElements").value(2));
                }

                @Test
                @DisplayName("GET /api/admin/users - 전체 회원 조회")
                void getAllUsers_empty() throws Exception {
                        // given
                        PageResponse<UserResponse> emptyResponse = PageResponse.<UserResponse>builder()
                                        .content(List.of())
                                        .pageNumber(0)
                                        .pageSize(10)
                                        .totalElements(0L)
                                        .totalPages(0)
                                        .first(true)
                                        .last(true)
                                        .build();

                        when(adminService.getAllUsers(any(Pageable.class))).thenReturn(emptyResponse);

                        // when & then
                        mockMvc.perform(get("/api/admin/users"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.content").isArray())
                                        .andExpect(jsonPath("$.data.content.length()").value(0));
                }

        @Test
        @DisplayName("전체 회원 조회 성공")
        void getAllUsers_withPaging() throws Exception {
            // given
            PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                    .content(List.of())
                    .pageNumber(1)
                    .pageSize(5)
                    .totalElements(15L)
                    .totalPages(3)
                    .first(false)
                    .last(false)
                    .build();

            when(adminService.getAllUsers(any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/users")
                            .param("page", "1")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
        }

        @Nested
        @DisplayName("GET /api/admin/users/{userId} - 회원 상세 조회")
        class GetUserByIdTest {

                @Test
                @DisplayName("GET /api/admin/users/{userId} - 회원 상세 조회")
                void getUserById_success() throws Exception {
                        // given
                        UserResponse response = createUserResponse(1L, "test@example.com", "USER");
                        when(adminService.getUserById(1L)).thenReturn(response);

                        // when & then
                        mockMvc.perform(get("/api/admin/users/1"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.id").value(1))
                                        .andExpect(jsonPath("$.data.email").value("test@example.com"));
                }

                @Test
                @DisplayName("GET /api/admin/users/{userId} - 회원 상세 조회")
                void getUserById_notFound_fail() throws Exception {
                        // given
                        when(adminService.getUserById(999L))
                                        .thenThrow(new UserDomainException(
                                                        UserDomainExceptionCode.UserNotFoundException));

                        // when & then
                        mockMvc.perform(get("/api/admin/users/999"))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("GET /api/admin/users/{userId} - 회원 상세 조회")
        class DeleteUserTest {

                @Test
                @DisplayName("GET /api/admin/users/{userId} - 회원 상세 조회")
                void deleteUser_success() throws Exception {
                        // given
                        doNothing().when(adminService).deleteUser(1L);

                        // when & then
                        mockMvc.perform(delete("/api/admin/users/1"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));

                        verify(adminService).deleteUser(1L);
                }

                @Test
                @DisplayName("GET /api/admin/users/{userId} - 회원 상세 조회")
                void deleteUser_notFound_fail() throws Exception {
                        // given
                        doThrow(new UserDomainException(UserDomainExceptionCode.UserNotFoundException))
                                        .when(adminService).deleteUser(999L);

                        // when & then
                        mockMvc.perform(delete("/api/admin/users/999"))
                                        .andExpect(status().isNotFound());
                }

                @Test
                @DisplayName("GET /api/admin/users/{userId} - 회원 상세 조회")
                void deleteUser_alreadyWithdrawn_fail() throws Exception {
                        // given
                        doThrow(new UserDomainException(UserDomainExceptionCode.UserAlreadyWithdrawnException))
                                        .when(adminService).deleteUser(1L);

                        // when & then
                        mockMvc.perform(delete("/api/admin/users/1"))
                                        .andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("GET /api/admin/users/{userId} - 회원 상세 조회")
        class ChangeUserRoleTest {

                @Test
                @DisplayName("GET /api/admin/users/{userId} - 회원 상세 조회")
                void changeUserRole_toAdmin_success() throws Exception {
                        // given
                        UserResponse response = createUserResponse(1L, "test@example.com", "ADMIN");
                        when(adminService.changeUserRole(anyLong(), anyString())).thenReturn(response);

                        // when & then
                        mockMvc.perform(put("/api/admin/users/1/role")
                                        .param("role", "ADMIN"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.role").value("ADMIN"));
                }

                @Test
                @DisplayName("GET /api/admin/users/{userId} - 회원 상세 조회")
                void changeUserRole_toUser_success() throws Exception {
                        // given
                        UserResponse response = createUserResponse(1L, "test@example.com", "USER");
                        when(adminService.changeUserRole(anyLong(), anyString())).thenReturn(response);

                        // when & then
                        mockMvc.perform(put("/api/admin/users/1/role")
                                        .param("role", "USER"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.role").value("USER"));
                }
        }

        private UserResponse createUserResponse(Long id, String email, String role) {
                return UserResponse.builder()
                        .id(id)
                        .email(email)
                        .name("테스트유저")
                        .phoneNumber("010-1234-5678")
                        .role(role)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .build();
        }
}
