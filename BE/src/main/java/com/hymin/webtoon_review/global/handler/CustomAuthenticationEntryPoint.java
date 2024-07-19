package com.hymin.webtoon_review.global.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.global.response.ErrorResponse;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException {
        ErrorResponse errorResponse = ErrorResponse.of(ResponseStatus.INVALID_JSON_WEB_TOKEN);
        ObjectMapper mapper = new ObjectMapper();

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(mapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}
