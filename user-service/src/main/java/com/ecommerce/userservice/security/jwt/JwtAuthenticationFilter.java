package com.ecommerce.userservice.security.jwt;

import com.ecommerce.userservice.enums.UserRole;
import com.ecommerce.userservice.security.CustomUserDetails;
import com.ecommerce.userservice.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            // 토큰 유효성 검증
            if (jwtTokenProvider.validateToken(token)) {
                // Access Token인지 확인
                if (!jwtTokenProvider.isAccessToken(token)) {
                    log.warn("Not an access token");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Blacklist 확인
                if (tokenService.isBlacklisted(token)) {
                    log.warn("Token is blacklisted");
                    filterChain.doFilter(request, response);
                    return;
                }

                // 인증 객체 생성 및 SecurityContext 설정
                Authentication authentication = createAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication set for user: {}", jwtTokenProvider.getUserId(token));
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 토큰 추출
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 토큰에서 인증 객체 생성
     */
    private Authentication createAuthentication(String token) {
        Long userId = jwtTokenProvider.getUserId(token);
        String email = jwtTokenProvider.getEmail(token);
        String roleKey = jwtTokenProvider.getRole(token);

        UserRole role = getUserRoleFromKey(roleKey);

        CustomUserDetails userDetails = new CustomUserDetails(userId, email, null, role);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    /**
     * Role key에서 UserRole enum 변환
     */
    private UserRole getUserRoleFromKey(String roleKey) {
        for (UserRole role : UserRole.values()) {
            if (role.getKey().equals(roleKey)) {
                return role;
            }
        }
        return UserRole.USER;
    }
}
