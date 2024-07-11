package com.hymin.webtoon_review.global.response;

import com.hymin.webtoon_review.util.Time;
import lombok.Getter;

@Getter
public class RestResponse {

    private Integer status;
    private String message;
    private String time;

    protected RestResponse(Integer status, String message) {
        this.status = status;
        this.message = message;
        this.time = Time.now();
    }

    public static RestResponse onCreated() {
        return new RestResponse(ResponseStatus.CREATED.getHttpStatusValue(),
            ResponseStatus.CREATED.getMessage());
    }
}
