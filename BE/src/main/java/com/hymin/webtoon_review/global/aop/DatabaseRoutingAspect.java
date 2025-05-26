package com.hymin.webtoon_review.global.aop;

import com.hymin.webtoon_review.global.DatabaseReadWriteRoutingDataSource;
import com.hymin.webtoon_review.global.async.Job;
import com.hymin.webtoon_review.global.async.JobQueue;
import com.hymin.webtoon_review.global.enums.DataSourceEnum;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@RequiredArgsConstructor
public class DatabaseRoutingAspect {

    private final JobQueue jobQueue;

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object setReadOnlyConnection(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Transactional transactional = AnnotationUtils.findAnnotation(signature.getMethod(),
            Transactional.class);

        if (transactional != null && transactional.readOnly()) {
            try {
                DatabaseReadWriteRoutingDataSource.setDataSourceKey(
                    DataSourceEnum.READ_ONLY.name());
                return joinPoint.proceed();
            } finally {
                DatabaseReadWriteRoutingDataSource.setDataSourceKey(DataSourceEnum.WRITE.name());
            }
        }
        return joinPoint.proceed();
    }

    @Around("@annotation(org.springframework.transaction.annotation.Transactional) && @annotation(org.springframework.scheduling.annotation.Async)")
    public Object setAsyncWriteOnlyConnection(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            DatabaseReadWriteRoutingDataSource.setDataSourceKey(
                DataSourceEnum.ASYNC_ONLY_WRITE.name());
            return joinPoint.proceed();
        } catch (DataAccessException e) {
            Object[] args = joinPoint.getArgs();

            String topic = null;
            List<Job<?>> jobs = null;

            for (Object arg : args) {
                if (arg instanceof String str) {
                    topic = str;
                } else if (
                    arg instanceof List<?> list &&
                        !list.isEmpty() &&
                        list.get(0) instanceof Job
                ) {
                    jobs = (List<Job<?>>) list;
                }
            }

            if (topic != null && jobs != null) {
                jobQueue.addRetry(topic, jobs);
            }

            throw e;
        } finally {
            DatabaseReadWriteRoutingDataSource.setDataSourceKey(DataSourceEnum.WRITE.name());
        }
    }
}
