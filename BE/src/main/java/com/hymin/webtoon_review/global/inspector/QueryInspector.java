package com.hymin.webtoon_review.global.inspector;

import org.hibernate.resource.jdbc.spi.StatementInspector;

public class QueryInspector implements StatementInspector {

    private final static String STRAIGHT_JOIN = "straight_join";

    @Override
    public String inspect(String sql) {
        if (sql.contains(STRAIGHT_JOIN)) {
            sql = sql.replace("join", "straight_join");
        }

        return sql;
    }
}
