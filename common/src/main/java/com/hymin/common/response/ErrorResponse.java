package com.hymin.common.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse extends BaseResponse {

    protected ErrorResponse(Integer status, String message) {
        super(status, message);
    }

    public static ErrorResponse of(HttpStatus httpStatus, String massage) {
        return new ErrorResponse(httpStatus.value(), massage);
    }
}
