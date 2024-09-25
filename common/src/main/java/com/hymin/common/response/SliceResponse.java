package com.hymin.common.response;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SliceResponse<T> extends DataResponse<T> {

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

    public static <T> SliceResponse<List<T>> onSuccess(List<T> data, Integer size) {
        boolean hasNext = data != null && data.size() - 1 == size;

        List<T> newList = new ArrayList<T>();
        newList.addAll(data);

        if (hasNext) {
            newList.remove(newList.size() - 1);
        }

        return new SliceResponse<>(
            HttpStatus.OK.value(),
            "정상 처리 됐습니다.",
            newList,
            hasNext,
            size,
            newList.size()
        );
    }
}
