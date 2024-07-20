package com.hymin.webtoon_review.global.security.authentication;

import java.util.Collection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtAuthentication extends UsernamePasswordAuthenticationToken {

    public JwtAuthentication(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public JwtAuthentication(Object principal, Object credentials,
        Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
