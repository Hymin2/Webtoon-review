package com.hymin.webtoon_review.global.security.provider;

import com.hymin.webtoon_review.global.security.UserDetailsImpl;
import com.hymin.webtoon_review.global.security.authentication.UsernamePasswordAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(
                username);

            if (!checkPassword(password, userDetails.getPassword())) {
                return new UsernamePasswordAuthentication(username, password);
            }

            UsernamePasswordAuthentication usernamePasswordAuthentication = new UsernamePasswordAuthentication(
                username, password, userDetails.getAuthorities());
            usernamePasswordAuthentication.setDetails(userDetails);

            return usernamePasswordAuthentication;
        } catch (UsernameNotFoundException e) {
            return new UsernamePasswordAuthentication(username, password);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthentication.class.isAssignableFrom(authentication);
    }

    private boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
