package com.ecommerce.userservice.controller;

import com.ecommerce.common.enums.UserRole;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ApiResponse<PageResponse<UserResponse>> getAllUsers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/admin/users - 전체 사용자 목록 조회");
        return ApiResponse.success(adminService.getAllUsers(pageable));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long userId) {
        log.info("GET /api/admin/users/{} - 사용자 상세 정보 조회", userId);
        return ApiResponse.success(adminService.getUserById(userId));
    }

    @DeleteMapping("/users/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        log.info("DELETE /api/admin/users/{} - 사용자 강제 탈퇴 처리", userId);
        adminService.deleteUser(userId);
        return ApiResponse.ok();
    }

    @PutMapping("/users/{userId}/role")
    public ApiResponse<UserResponse> changeUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        log.info("PUT /api/admin/users/{}/role - 사용자 권한 변경 요청 role={}", userId, role);
        return ApiResponse.success(adminService.changeUserRole(userId, role));
    }
}
