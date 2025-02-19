package com.hymin.webtoon_review.global.async;

import com.hymin.webtoon_review.util.Time;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Job<T> {

    private String jobId;
    private String timestamp;
    private T data;

    public static <T> Job<T> of(T data) {
        return new Job<T>(UUID.randomUUID().toString(), Time.now(), data);
    }
}
