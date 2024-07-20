package com.hymin.webtoon_review.global.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.global.response.ErrorResponse;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ErrorResponse errorResponse = ErrorResponse.of(ResponseStatus.FORBIDDEN);
        ObjectMapper mapper = new ObjectMapper();

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        response.getWriter().write(mapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}
