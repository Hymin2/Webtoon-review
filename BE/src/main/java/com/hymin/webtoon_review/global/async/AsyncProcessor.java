package com.hymin.webtoon_review.global.async;

import java.util.List;

public interface AsyncProcessor {

    void process(List<Job<?>> jobs);
}
