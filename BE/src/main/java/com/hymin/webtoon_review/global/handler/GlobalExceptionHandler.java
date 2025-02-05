package com.hymin.webtoon_review.global.handler;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ErrorResponse;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return createResponseEntity(ex, ErrorResponse.of(ResponseStatus.INVALID_INPUT), headers,
            status,
            request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleEtcException(Exception ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        
        return createResponseEntity(ex,
            ErrorResponse.of(ResponseStatus.INTERNAL_SERVER_ERROR),
            HttpHeaders.EMPTY,
            ResponseStatus.INTERNAL_SERVER_ERROR.getHttpStatus(),
            request);
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<Object> handleGeneralException(final GeneralException e,
        final WebRequest request) {
        return createResponseEntity(e,
            ErrorResponse.of(e.getResponseStatus()),
            HttpHeaders.EMPTY,
            e.getResponseStatus().getHttpStatus(),
            request
        );
    }

    private ResponseEntity<Object> createResponseEntity(Exception e, Object body,
        HttpHeaders headers,
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
