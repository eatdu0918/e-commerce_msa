package com.ecommerce.userservice.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private AccessToken accessToken = new AccessToken();
    private RefreshToken refreshToken = new RefreshToken();

    @Getter
    @Setter
    public static class AccessToken {
        private long expiration = 1800000; // 30분 (밀리초)
    }

    @Getter
    @Setter
    public static class RefreshToken {
        private long expiration = 604800000; // 7일 (밀리초)
    }

    public long getAccessTokenExpirationMs() {
        return accessToken.getExpiration();
    }

    public long getRefreshTokenExpirationMs() {
        return refreshToken.getExpiration();
    }

    public long getAccessTokenExpirationSeconds() {
        return accessToken.getExpiration() / 1000;
    }

    public long getRefreshTokenExpirationSeconds() {
        return refreshToken.getExpiration() / 1000;
    }
}
