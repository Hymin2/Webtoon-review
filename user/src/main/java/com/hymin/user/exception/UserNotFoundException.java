package com.hymin.user.exception;

import com.hymin.common.exception.GeneralException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends GeneralException {

    public UserNotFoundException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
