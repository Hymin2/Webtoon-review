package com.hymin.webtoon_review.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.global.security.authentication.UsernamePasswordAuthentication;
import com.hymin.webtoon_review.user.dto.UserRequest.LoginInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class UsernamePasswordAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        LoginInfo loginInfo = objectMapper.readValue(request.getReader(), LoginInfo.class);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthentication(
                loginInfo.getUsername(),
                loginInfo.getPassword()
            ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getServletPath().equals("/users/login");
    }
}
