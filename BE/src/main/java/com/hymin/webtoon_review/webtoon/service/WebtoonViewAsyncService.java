package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.async.AsyncProcessor;
import com.hymin.webtoon_review.global.async.Job;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebtoonViewAsyncService implements AsyncProcessor {

    private final JdbcTemplate jdbcTemplate;

    @Async
    @Override
    public void process(List<Job<?>> jobs) {
        String sql = "UPDATE webtoon SET views = views + 1 WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, jobs, jobs.size(), (ps, job) -> {
            ps.setLong(1, (Long) job.getData());
        });
    }
}
