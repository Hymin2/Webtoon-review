package com.hymin.webtoon_review.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Queue {

    String topic();

    int threshold() default 1;

    Class<? extends Exception>[] retry() default {};

    int retryLimit() default 0;
}
