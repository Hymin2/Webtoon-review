package com.hymin.webtoon_review.global.response;

import com.hymin.webtoon_review.util.Time;
import lombok.Getter;

@Getter
public class SliceResponse<T> extends ApiResponse {

    private Boolean hasNext;
    private Integer count;
    private Integer nextId;

    protected SliceResponse(Integer status, String message, String time, T data, Boolean hasNext,
        Integer count, Integer nextId) {
        super(status, message, time, data);
        this.hasNext = hasNext;
        this.count = count;
        this.nextId = nextId;
    }

    public static <T> SliceResponse<T> onSuccess(T data, Boolean hasNext, Integer count,
        Integer nextId) {
        return new SliceResponse<>(
            ResponseStatus.OK.getStatus(),
            ResponseStatus.OK.getMessage(),
            Time.now(),
            data,
            hasNext,
            count,
            nextId
        );
    }
}
