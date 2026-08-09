package com.hymin.webtoon_review.global.queue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum FileName {
    OFFLINE_LOG_FILE_NAME("_offline.log", "offline"),
    TOPIC_FILE_NAME("topics.txt", ""),
    QUEUE_DATE_FILE_NAME("_queue_data.log", ""),
    TEMP_DATA_FILE_NAME("_temp_data.log", ""),
    ERROR_LOG_FILE_NAME("_error_data.log", "error"),
    ;

    private final String name;
    @Getter
    private final String directory;

    public String getName(String topic) {
        return topic + name;
    }

    public String getName() {
        return name;
    }
}
