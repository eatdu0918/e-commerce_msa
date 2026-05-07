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
        log.info("GET /api/admin/users - ?    ???     ??);
        return ApiResponse.success(adminService.getAllUsers(pageable));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long userId) {
        log.info("GET /api/admin/users/{} - ???  ?       ??, userId);
        return ApiResponse.success(adminService.getUserById(userId));
    }

    @DeleteMapping("/users/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        log.info("DELETE /api/admin/users/{} - ???     ????  ", userId);
        adminService.deleteUser(userId);
        return ApiResponse.ok();
    }

    @PutMapping("/users/{userId}/role")
    public ApiResponse<UserResponse> changeUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        log.info("PUT /api/admin/users/{}/role - ???      ?    ? role={}", userId, role);
        return ApiResponse.success(adminService.changeUserRole(userId, role));
    }
}
