package com.hymin.webtoon_review.global.response;

import com.hymin.webtoon_review.util.Time;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {

    private Integer status;
    private String message;
    private String time;
    private T data;

    public static <T> ApiResponse<T> onSuccess(T data) {
        return ApiResponse.of(ResponseStatus.OK, data);
    }

    public static <T> ApiResponse<T> of(ResponseStatus responseStatus, T data) {
        return ApiResponse.
            <T>builder()
            .time(Time.now())
            .status(responseStatus.getStatus())
            .message(responseStatus.getMessage())
            .data(data)
            .build();
    }
}
