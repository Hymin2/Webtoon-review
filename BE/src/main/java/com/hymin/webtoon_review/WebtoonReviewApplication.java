package com.hymin.webtoon_review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WebtoonReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebtoonReviewApplication.class, args);
    }

}
