package com.hymin.webtoon_review.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

@Configuration
public class MongoTransactionConfiguration {

    public static final String TRANSACTION_MANAGER = "mongoTransactionManager";

    @Bean(name = TRANSACTION_MANAGER)
    public MongoTransactionManager mongoTransactionManager(
        MongoDatabaseFactory mongoDatabaseFactory
    ) {
        return new MongoTransactionManager(mongoDatabaseFactory);
    }
}
