package com.hymin.webtoon_review.global.aop;

import com.hymin.webtoon_review.global.annotation.Queue;
import com.hymin.webtoon_review.global.queue.Job;
import com.hymin.webtoon_review.global.queue.JobQueue;
import com.hymin.webtoon_review.global.queue.QueueLogArchiver;
import com.hymin.webtoon_review.global.queue.QueueLogWriter;
import com.hymin.webtoon_review.global.queue.QueueTemplate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class QueueProcessorAspect {

    private final QueueLogArchiver queueLogArchiver;
    private final QueueTemplate queueTemplate;
    private final JobQueue jobQueue;
    private final QueueLogWriter queueLogWriter;

    @Around("execution(* com.hymin.webtoon_review.global.queue.QueueProcessor+.process(..))")
    public Object aroundProcess(ProceedingJoinPoint joinPoint) throws Throwable {
        Class<?> clazz = joinPoint.getTarget().getClass();
        Queue annotation = clazz.getAnnotation(Queue.class);

        List<Job<?>> jobs = (List<Job<?>>) joinPoint.getArgs()[0];
        String topic = annotation.topic();
        Class<? extends Exception>[] retry = annotation.retry();

        try {
            joinPoint.proceed();
        } catch (Exception e) {
            boolean retryable = false;

            for (Class<? extends Exception> retryClass : retry) {
                if (retryClass.isAssignableFrom(e.getClass())) {
                    retryable = true;
                    break;
                }
            }

            if (retryable) {
                jobs.forEach(job -> {
                    if (jobQueue.isRetryable(topic, job.getRetryCount())) {
                        job.increaseRetryCount();
                        queueTemplate.add(topic, job);
                    } else {
                        queueLogWriter.appendErrorLog(topic, job);
                    }
                });
            }
        }

        queueLogArchiver.archive(topic, jobs.size());

        return null;
    }
}
