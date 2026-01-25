package com.hymin.webtoon_review.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.global.response.ErrorResponse;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String[] privateGetUri = {};
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        String accessToken = request.getHeader("Authorization");

        if (userService.isBlacklisted(accessToken)) {
            handleException(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (!"GET".equalsIgnoreCase(method)) {
            return false;
        }

        return Arrays.stream(privateGetUri)
            .noneMatch(pattern -> pathMatcher.match(pattern, uri));
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
