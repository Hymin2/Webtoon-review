package com.hymin.webtoon_review.global.response;

import java.util.List;
import lombok.Getter;

@Getter
public class SliceResponse<T extends List> extends ApiResponse {

    private Boolean hasNext;
    private Integer size;
    private Integer count;

    protected SliceResponse(Integer status, String message, T data, Boolean hasNext, Integer size,
        Integer count) {
        super(status, message, data);
        this.hasNext = hasNext;
        this.size = size;
        this.count = count;
    }

    public static <T extends List> SliceResponse<T> onSuccess(T data, Integer size) {
        boolean hasNext = data != null && data.size() - 1 == size;

        if (hasNext) {
            data.remove(data.size() - 1);
        }

        return new SliceResponse<>(
            ResponseStatus.OK.getHttpStatusValue(),
            ResponseStatus.OK.getMessage(),
            data,
            hasNext,
            size,
            data.size()
        );
    }
}
