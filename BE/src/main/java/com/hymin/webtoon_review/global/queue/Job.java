package com.hymin.webtoon_review.global.queue;

import com.hymin.webtoon_review.util.Time;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Job<T> {

    private String jobId;
    private String timestamp;
    private T data;
    private Integer retryCount;

    public static <T> Job<T> of(T data) {
        return new Job<T>(UUID.randomUUID().toString(), Time.now(), data, 0);
    }

    public void increaseRetryCount() {
        retryCount++;
    }
}
