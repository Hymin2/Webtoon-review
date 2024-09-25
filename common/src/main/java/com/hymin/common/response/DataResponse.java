package com.hymin.common.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DataResponse<T> extends BaseResponse{

    private T data;

    protected DataResponse(Integer status, String message, T data) {
        super(status, message);
        this.data = data;
    }

    public static <T> DataResponse<T> onSuccess(T data) {
        return DataResponse.of(HttpStatus.OK, "정상 처리 됐습니다.", data);
    }

    public static <T> DataResponse<T> of(HttpStatus httpStatus, String message, T data) {
        return new DataResponse<>(
            httpStatus.value(),
            message,
            data
        );
    }
}
