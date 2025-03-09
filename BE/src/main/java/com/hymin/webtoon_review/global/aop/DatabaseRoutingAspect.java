package com.hymin.webtoon_review.global.aop;

import com.hymin.webtoon_review.global.DatabaseReadWriteRoutingDataSource;
import com.hymin.webtoon_review.global.enums.DataSourceEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
public class DatabaseRoutingAspect {

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
        } finally {
            DatabaseReadWriteRoutingDataSource.setDataSourceKey(DataSourceEnum.WRITE.name());
        }
    }
}
