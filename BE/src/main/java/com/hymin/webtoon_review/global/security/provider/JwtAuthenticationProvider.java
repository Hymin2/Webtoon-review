package com.hymin.webtoon_review.global.security.provider;

import com.hymin.webtoon_review.global.security.JwtService;
import com.hymin.webtoon_review.global.security.authentication.JwtAuthentication;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtService jwtService;

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        String jwt = authentication.getCredentials().toString();

        try {
            Claims claims = jwtService.parseJwt(jwt);

            String username = claims.getSubject();
            List<SimpleGrantedAuthority> authorities = Arrays.stream(
                    claims.get("authorities", String.class).split(", "))
                .map(SimpleGrantedAuthority::new)
                .toList();

            return new JwtAuthentication(username, "", authorities);
        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException("만료된 토큰입니다.", e);
        } catch (JwtException e) {
            throw new BadCredentialsException("유효하지 않는 토큰입니다.", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
}
