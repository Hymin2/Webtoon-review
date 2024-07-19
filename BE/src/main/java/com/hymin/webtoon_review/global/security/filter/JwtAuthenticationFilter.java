package com.hymin.webtoon_review.global.security.filter;

import com.hymin.webtoon_review.global.security.authentication.JwtAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;

    private final String[] notFilters = {
        "/users/login",
        "/users",
        "/users/check/username",
        "/users/check/nickname"};

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            String auth = request.getHeader("Authorization");

            Authentication authentication = authenticationManager.authenticate(
                new JwtAuthentication("", auth));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException e) {

        } finally {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        boolean b = false;

        for (String notFilter : notFilters) {
            b = b || request.getRequestURI().contains(notFilter);
        }

        return b;
    }
}
