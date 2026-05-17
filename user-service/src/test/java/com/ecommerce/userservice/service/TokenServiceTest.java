package com.ecommerce.userservice.service;

import com.ecommerce.common.security.JwtProperties;
import com.ecommerce.common.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @Mock
    JwtProperties jwtProperties;

    @InjectMocks
    TokenService tokenService;

    private static final Long USER_ID = 1L;
    private static final String REFRESH_TOKEN = "testRefreshToken";
    private static final String ACCESS_TOKEN = "testAccessToken";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final long EXPIRATION_SECONDS = 604800L; // 7 days

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("Refresh Token 저장 테스트")
    class SaveRefreshTokenTest {

        @Test
        @DisplayName("Refresh Token 저장 성공")
        void saveRefreshToken_success() {
            // given
            when(jwtProperties.getRefreshTokenExpirationSeconds()).thenReturn(EXPIRATION_SECONDS);

            // when
            tokenService.saveRefreshToken(USER_ID, REFRESH_TOKEN);

            // then
            verify(valueOperations).set(
                    REFRESH_TOKEN_PREFIX + USER_ID,
                    REFRESH_TOKEN,
                    EXPIRATION_SECONDS,
                    TimeUnit.SECONDS
            );
        }
    }

    @Nested
    @DisplayName("Refresh Token 조회 테스트")
    class GetRefreshTokenTest {

        @Test
        @DisplayName("Refresh Token 조회 성공")
        void getRefreshToken_success() {
            // given
            when(valueOperations.get(REFRESH_TOKEN_PREFIX + USER_ID)).thenReturn(REFRESH_TOKEN);

            // when
            String result = tokenService.getRefreshToken(USER_ID);

            // then
            assertThat(result).isEqualTo(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("Refresh Token 조회 실패 - 존재하지 않음")
        void getRefreshToken_notFound_returnsNull() {
            // given
            when(valueOperations.get(REFRESH_TOKEN_PREFIX + USER_ID)).thenReturn(null);

            // when
            String result = tokenService.getRefreshToken(USER_ID);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Refresh Token 삭제 테스트")
    class DeleteRefreshTokenTest {

        @Test
        @DisplayName("Refresh Token 삭제 성공")
        void deleteRefreshToken_success() {
            // when
            tokenService.deleteRefreshToken(USER_ID);

            // then
            verify(redisTemplate).delete(REFRESH_TOKEN_PREFIX + USER_ID);
        }
    }

    @Nested
    @DisplayName("Access Token 블랙리스트 테스트")
    class BlacklistTest {

        @Test
        @DisplayName("Access Token 블랙리스트 추가 성공")
        void addToBlacklist_success() {
            // given
            long expirationSeconds = 3600L;

            // when
            tokenService.addToBlacklist(ACCESS_TOKEN, expirationSeconds);

            // then
            verify(valueOperations).set(
                    BLACKLIST_PREFIX + ACCESS_TOKEN,
                    "logout",
                    expirationSeconds,
                    TimeUnit.SECONDS
            );
        }

        @Test
        @DisplayName("Access Token 블랙리스트 추가 - 만료된 토큰은 추가 안함")
        void addToBlacklist_expiredToken_noAction() {
            // given
            long expirationSeconds = 0L;

            // when
            tokenService.addToBlacklist(ACCESS_TOKEN, expirationSeconds);

            // then
            verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        }

        @Test
        @DisplayName("Access Token 블랙리스트 추가 - 음수 만료 시간")
        void addToBlacklist_negativeExpiration_noAction() {
            // given
            long expirationSeconds = -100L;

            // when
            tokenService.addToBlacklist(ACCESS_TOKEN, expirationSeconds);

            // then
            verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        }

        @Test
        @DisplayName("블랙리스트 확인 - 존재함")
        void isBlacklisted_exists_returnsTrue() {
            // given
            when(redisTemplate.hasKey(BLACKLIST_PREFIX + ACCESS_TOKEN)).thenReturn(true);

            // when
            boolean result = tokenService.isBlacklisted(ACCESS_TOKEN);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("블랙리스트 확인 - 존재하지 않음")
        void isBlacklisted_notExists_returnsFalse() {
            // given
            when(redisTemplate.hasKey(BLACKLIST_PREFIX + ACCESS_TOKEN)).thenReturn(false);

            // when
            boolean result = tokenService.isBlacklisted(ACCESS_TOKEN);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("블랙리스트 확인 - null 반환시 false")
        void isBlacklisted_null_returnsFalse() {
            // given
            when(redisTemplate.hasKey(BLACKLIST_PREFIX + ACCESS_TOKEN)).thenReturn(null);

            // when
            boolean result = tokenService.isBlacklisted(ACCESS_TOKEN);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Refresh Token 검증 테스트")
    class ValidateRefreshTokenTest {

        @Test
        @DisplayName("Refresh Token 검증 성공 - 일치")
        void validateRefreshToken_matches_returnsTrue() {
            // given
            when(valueOperations.get(REFRESH_TOKEN_PREFIX + USER_ID)).thenReturn(REFRESH_TOKEN);

            // when
            boolean result = tokenService.validateRefreshToken(USER_ID, REFRESH_TOKEN);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Refresh Token 검증 실패 - 불일치")
        void validateRefreshToken_mismatch_returnsFalse() {
            // given
            when(valueOperations.get(REFRESH_TOKEN_PREFIX + USER_ID)).thenReturn("differentToken");

            // when
            boolean result = tokenService.validateRefreshToken(USER_ID, REFRESH_TOKEN);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Refresh Token 검증 실패 - 저장된 토큰 없음")
        void validateRefreshToken_notStored_returnsFalse() {
            // given
            when(valueOperations.get(REFRESH_TOKEN_PREFIX + USER_ID)).thenReturn(null);

            // when
            boolean result = tokenService.validateRefreshToken(USER_ID, REFRESH_TOKEN);

            // then
            assertThat(result).isFalse();
        }
    }
}