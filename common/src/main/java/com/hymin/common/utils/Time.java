package com.hymin.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Time {

    public static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
