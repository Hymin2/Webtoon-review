package com.hymin.common.response;

import com.hymin.common.utils.Time;
import lombok.Getter;

@Getter
public class BaseResponse {

    private Integer status;
    private String message;
    private String time;

    protected BaseResponse(Integer status, String message) {
        this.status = status;
        this.message = message;
        this.time = Time.now();
    }
}
