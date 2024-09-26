package com.hymin.user.exception;

import com.hymin.common.exception.GeneralException;
import org.springframework.http.HttpStatus;

public class UsernameAlreadyExistsException extends GeneralException {

    public UsernameAlreadyExistsException(HttpStatus httpStatus,
        String message) {
        super(httpStatus, message);
    }
}
