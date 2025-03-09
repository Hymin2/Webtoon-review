package com.hymin.webtoon_review.global;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DatabaseReadWriteRoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void setDataSourceKey(String key) {
        CONTEXT.set(key);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return CONTEXT.get();
    }
}
