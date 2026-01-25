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
        try {
            String path = (String) authentication.getDetails();
            String jwt = authentication.getCredentials().toString();
            Claims claims = jwtService.parseJwt(jwt);

            if (path.equals("/users/refresh")) {
                throw new BadCredentialsException("토큰이 아직 만료되지 않았습니다. 만료 후에 재발급이 가능합니다.");
            }

            return createJwtAuthentication(claims);
        } catch (ExpiredJwtException e) {
            String path = (String) authentication.getDetails();

            if (path.equals("/users/refresh")) {
                Claims claims = e.getClaims();
                return createJwtAuthentication(claims);
            }

            throw new BadCredentialsException("만료된 토큰입니다.", e);
        } catch (JwtException | NullPointerException e) {
            throw new BadCredentialsException("유효하지 않는 토큰입니다.", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }

    private JwtAuthentication createJwtAuthentication(Claims claims) {
        String username = claims.getSubject();
        List<SimpleGrantedAuthority> authorities = Arrays.stream(
                claims.get("authorities", String.class).split(", "))
            .map(SimpleGrantedAuthority::new)
            .toList();

        JwtAuthentication jwtAuthentication = new JwtAuthentication(username, "", authorities);
        jwtAuthentication.setDetails(claims.get("id"));

        return jwtAuthentication;
    }
}
