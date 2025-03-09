package com.hymin.webtoon_review.global.config;

import com.hymin.webtoon_review.global.DatabaseReadWriteRoutingDataSource;
import com.hymin.webtoon_review.global.enums.DataSourceEnum;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.read")
    public DataSource readOnlyDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setReadOnly(true);
        return dataSource;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSource writeDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        return dataSource;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.async-write")
    public DataSource asyncWriteDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        return dataSource;
    }

    @Bean
    public DataSource routingDataSource(
        @Qualifier("readOnlyDataSource") DataSource readOnlyDataSource,
        @Qualifier("writeDataSource") DataSource writeDataSource,
        @Qualifier("asyncWriteDataSource") DataSource asyncWriteDataSource) {
        DatabaseReadWriteRoutingDataSource routingDataSource = new DatabaseReadWriteRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();

        targetDataSources.put(DataSourceEnum.READ_ONLY.name(), readOnlyDataSource);
        targetDataSources.put(DataSourceEnum.WRITE.name(), writeDataSource);
        targetDataSources.put(DataSourceEnum.ASYNC_ONLY_WRITE.name(), asyncWriteDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(writeDataSource);
        return routingDataSource;
    }

    @Bean
    @Primary
    public DataSource dataSource(
        @Qualifier("routingDataSource") DataSource routingDataSource
    ) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(
        EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
        return jpaTransactionManager;
    }
}
