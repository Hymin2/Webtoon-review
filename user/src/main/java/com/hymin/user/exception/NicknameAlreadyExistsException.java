package com.hymin.user.exception;

import com.hymin.common.exception.GeneralException;
import org.springframework.http.HttpStatus;

public class NicknameAlreadyExistsException extends GeneralException {

    public NicknameAlreadyExistsException(HttpStatus httpStatus,
        String message) {
        super(httpStatus, message);
    }
}
