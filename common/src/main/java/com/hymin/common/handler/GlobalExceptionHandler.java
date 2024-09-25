package com.hymin.common.handler;

import com.hymin.common.exception.GeneralException;
import com.hymin.common.response.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return createResponseEntity(ex, ErrorResponse.of(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."), headers,
            status,
            request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleEtcException(Exception ex, WebRequest request) {
        return createResponseEntity(ex,
            ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다."),
            HttpHeaders.EMPTY,
            HttpStatus.INTERNAL_SERVER_ERROR,
            request);
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<Object> handleGeneralException(final GeneralException e,
        final WebRequest request) {
        return createResponseEntity(e,
            ErrorResponse.of(e.getHttpStatus(), e.getMessage()),
            HttpHeaders.EMPTY,
            e.getHttpStatus(),
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
