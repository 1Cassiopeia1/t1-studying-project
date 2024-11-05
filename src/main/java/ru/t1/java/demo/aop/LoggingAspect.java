package ru.t1.java.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Async
@Slf4j
@Aspect
@Component
@Order(0)
public class LoggingAspect {

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logControllerExecTime(ProceedingJoinPoint pJoinPoint) {
        log.info("Вызов метода контроллера: {}", pJoinPoint.getSignature().toShortString());
        long beforeTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = pJoinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("Ошибка при при вызове метода {}",
                    pJoinPoint.getSignature().toShortString(), throwable);
        }
        long afterTime = System.currentTimeMillis();
        log.info("Время исполнения метода {} контроллера: {} мс",
                pJoinPoint.getSignature().toShortString(), (afterTime - beforeTime));
        return result;
    }

}
