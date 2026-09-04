package com.hymin.webtoon_review.chat.server.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hymin.webtoon_review.chat.server.service.CreateChatMessageCommandService;
import com.hymin.webtoon_review.global.config.DataSourceConfig;
import com.hymin.webtoon_review.global.config.MongoTransactionConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.MethodParameter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

class TransactionManagerConfigurationTest {

    @Test
    void selectsJpaByDefaultAndMongoExplicitlyWhenBothManagersAreRegistered() {
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction entityTransaction = mock(EntityTransaction.class);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.getTransaction()).thenReturn(entityTransaction);

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(EntityManagerFactory.class, () -> entityManagerFactory);
            context.registerBean(MongoDatabaseFactory.class, () -> mock(MongoDatabaseFactory.class));
            context.register(
                DataSourceConfig.class,
                MongoTransactionConfiguration.class,
                TransactionalProbeConfiguration.class
            );
            context.refresh();

            assertThat(context.getBean(PlatformTransactionManager.class))
                .isInstanceOf(JpaTransactionManager.class);
            assertThat(context.getBean("transactionManager", PlatformTransactionManager.class))
                .isSameAs(context.getBean(PlatformTransactionManager.class));
            PlatformTransactionManager mongoTransactionManager = context.getBean(
                MongoTransactionConfiguration.TRANSACTION_MANAGER,
                PlatformTransactionManager.class
            );
            assertThat(mongoTransactionManager).isInstanceOf(MongoTransactionManager.class);
            assertThat(resolveCommandServiceTransactionManager(context))
                .isSameAs(mongoTransactionManager);

            context.getBean(JpaTransactionalProbe.class).execute();
            verify(entityTransaction).begin();
            verify(entityTransaction).commit();
        }
    }

    private Object resolveCommandServiceTransactionManager(
        AnnotationConfigApplicationContext context
    ) {
        Constructor<?> constructor = CreateChatMessageCommandService.class.getConstructors()[0];
        MethodParameter transactionManagerParameter = new MethodParameter(constructor, 3);
        return context.getAutowireCapableBeanFactory().resolveDependency(
            new DependencyDescriptor(transactionManagerParameter, true),
            null
        );
    }

    @Configuration
    @EnableTransactionManagement
    static class TransactionalProbeConfiguration {

        @Bean
        JpaTransactionalProbe jpaTransactionalProbe() {
            return new JpaTransactionalProbe();
        }
    }

    static class JpaTransactionalProbe {

        @Transactional
        void execute() {
        }
    }
}
