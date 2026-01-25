package com.hymin.webtoon_review.global.response;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class SliceResponse<T> extends ApiResponse<T> {

    private Boolean hasNext;
    private Integer size;
    private Integer count;
    private String next;

    protected SliceResponse(Integer status, String message, T data, Boolean hasNext, Integer size,
        Integer count) {
        super(status, message, data);
        this.hasNext = hasNext;
        this.size = size;
        this.count = count;
    }

    protected SliceResponse(Integer status, String message, T data, Boolean hasNext, Integer size,
        Integer count, String next) {
        super(status, message, data);
        this.hasNext = hasNext;
        this.size = size;
        this.count = count;
        this.next = next;
    }

    public static <T> SliceResponse<List<T>> onSuccess(List<T> data, Integer size) {
        boolean hasNext = data != null && data.size() - 1 == size;

        List<T> newList = new ArrayList<T>();
        newList.addAll(data);

        if (hasNext) {
            newList.remove(newList.size() - 1);
        }

        return new SliceResponse<>(
            ResponseStatus.OK.getHttpStatusValue(),
            ResponseStatus.OK.getMessage(),
            newList,
            hasNext,
            size,
            newList.size()
        );
    }

    public static <T> SliceResponse<List<T>> onSuccess(List<T> data, Integer size, String next) {
        boolean hasNext = data != null && data.size() - 1 == size;

        List<T> newList = new ArrayList<T>();
        newList.addAll(data);

        if (hasNext) {
            newList.remove(newList.size() - 1);

            return new SliceResponse<>(
                ResponseStatus.OK.getHttpStatusValue(),
                ResponseStatus.OK.getMessage(),
                newList,
                hasNext,
                size,
                newList.size(),
                next
            );
        }

        return new SliceResponse<>(
            ResponseStatus.OK.getHttpStatusValue(),
            ResponseStatus.OK.getMessage(),
            newList,
            hasNext,
            size,
            newList.size()
        );
    }
}
