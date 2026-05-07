package com.ecommerce.common.service;

import com.ecommerce.common.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String LOGOUT_VALUE = "logout";

    /**
     * Refresh Token ????     */
    public void saveRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        long expirationSeconds = jwtProperties.getRefreshTokenExpirationSeconds();

        redisTemplate.opsForValue().set(key, refreshToken, expirationSeconds, TimeUnit.SECONDS);
        log.debug("Refresh token saved for userId: {}", userId);
    }

    /**
     * Refresh Token    ??
     */
    public String getRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Refresh Token ????(   ??    ??
     */
    public void deleteRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Refresh token deleted for userId: {}", userId);
    }

    /**
     * Access Token Blacklist ?  ?
     */
    public void addToBlacklist(String accessToken, long expirationSeconds) {
        if (expirationSeconds <= 0) {
            return;
        }
        String key = BLACKLIST_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, LOGOUT_VALUE, expirationSeconds, TimeUnit.SECONDS);
        log.debug("Access token added to blacklist");
    }

    /**
     * Blacklist ?   
     */
    public boolean isBlacklisted(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Refresh Token ?   ??    ?(???  ??   ????   ???)
     */
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String storedToken = getRefreshToken(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }
}
