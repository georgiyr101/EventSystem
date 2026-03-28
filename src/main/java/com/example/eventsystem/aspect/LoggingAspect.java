package com.example.eventsystem.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.example.eventsystem.service..*(..))")
    public Object logExecutionTime(final ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            log.info("Method {} executed in {} ms",
                    joinPoint.getSignature().toShortString(),
                    executionTime);
        }
    }
}