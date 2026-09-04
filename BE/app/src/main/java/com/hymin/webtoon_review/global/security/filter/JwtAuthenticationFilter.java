package com.hymin.webtoon_review.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.global.response.ErrorResponse;
import com.hymin.webtoon_review.global.response.ResponseStatus;
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

    private final ObjectMapper objectMapper;
    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            String auth = request.getHeader("Authorization");

            JwtAuthentication token = new JwtAuthentication("", auth);
            token.setDetails(path);

            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            String path = request.getRequestURI();

            if (path.equals("/users/refresh")) {
                handleException(response);
                return;
            }

            filterChain.doFilter(request, response);
        }
    }

    private void handleException(HttpServletResponse response)
        throws IOException {
        ErrorResponse errorResponse = ErrorResponse.of(ResponseStatus.INVALID_TOKEN);
        String json = objectMapper.writeValueAsString(errorResponse);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
    }
}
