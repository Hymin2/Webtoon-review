package com.hymin.webtoon_review.global.handler;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<?> handleGeneralException(final GeneralException e,
        final WebRequest request) {
        return createResponseEntity(e,
            ErrorResponse.of(e.getResponseStatus()),
            HttpHeaders.EMPTY,
            e.getResponseStatus().getHttpStatus(),
            request
        );
    }

    private ResponseEntity<?> createResponseEntity(Exception e, Object body, HttpHeaders headers,
        HttpStatusCode httpStatusCode, WebRequest request) {
        return super.handleExceptionInternal(
            e,
            body,
            headers,
            httpStatusCode,
            request
        );
    }
}
