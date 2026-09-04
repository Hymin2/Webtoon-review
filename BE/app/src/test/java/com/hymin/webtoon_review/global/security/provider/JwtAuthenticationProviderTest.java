package com.hymin.webtoon_review.global.security.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hymin.webtoon_review.global.security.JwtService;
import com.hymin.webtoon_review.global.security.authentication.JwtAuthentication;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class JwtAuthenticationProviderTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtService);

    @Test
    void JWT_사용자_ID가_Integer여도_Long으로_변환한다() {
        Claims claims = mock(Claims.class);
        when(jwtService.parseJwt("access-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("chat_test_user_a");
        when(claims.get("authorities", String.class)).thenReturn("ROLE_USER");
        when(claims.get("id")).thenReturn(1);

        JwtAuthentication request = new JwtAuthentication("", "access-token");
        request.setDetails("/chat/room");

        Authentication authenticated = provider.authenticate(request);

        assertThat(authenticated.getDetails())
            .isInstanceOf(Long.class)
            .isEqualTo(1L);
    }
}
