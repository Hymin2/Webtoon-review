package com.hymin.webtoon_review.user.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenDto {

    private String refreshToken;
    private String prevRefreshToken;
    private String accessToken;
    private long expirationTime;
    private Integer retryCount;

    public void refresh(String newRefreshToken, String newAccessToken, long expirationTime) {
        this.prevRefreshToken = this.refreshToken;
        this.refreshToken = newRefreshToken;
        this.accessToken = newAccessToken;
        this.expirationTime = expirationTime;
    }

    public void retry() {
        retryCount++;
    }

    public boolean checkRetry(String refreshToken) {
        Instant now = Instant.now();
        Instant expiration = Instant.ofEpochMilli(this.expirationTime);

        return this.prevRefreshToken.equals(refreshToken) &&
            this.retryCount <= 3 &&
            now.isBefore(expiration);
    }

    public boolean checkValid(String refreshToken) {
        return refreshToken.equals(this.refreshToken) ||
            refreshToken.equals(this.prevRefreshToken);
    }

    public boolean checkReused(String refreshToken) {
        Instant now = Instant.now();
        Instant expiration = Instant.ofEpochMilli(this.expirationTime);

        if (refreshToken.equals(prevRefreshToken) &&
            now.isBefore(expiration) &&
            retryCount > 3) {
            return true;
        } else if (refreshToken.equals(prevRefreshToken) &&
            now.isAfter(expiration)) {
            return true;
        }

        return false;
    }

    public boolean equalsAccessToken(String accessToken) {
        return this.accessToken.equals(accessToken);
    }
}
