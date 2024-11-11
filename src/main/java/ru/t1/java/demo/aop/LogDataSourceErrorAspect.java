package ru.t1.java.demo.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.exception.JpaException;
import ru.t1.java.demo.exception.JpaNotFoundException;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class LogDataSourceErrorAspect {

    private static final String NOT_FOUND_MESSAGE = "Сущность не найдена по указанным параметрам";
    private static final String LINE_BREAK = "\n";

    private final DataSourceErrorLogRepository dataSourceErrorLogRepository;

    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void beanAnnotatedWithRepository() {
    }

    @Pointcut("execution(* org.springframework.data.repository.CrudRepository.*(..))")
    public void anyCrudRepositoryMethod() {
    }

    @Pointcut("this(org.springframework.data.repository.CrudRepository)")
    public void anyProxyImplementingCrudRepository() {
    }

    @Pointcut("@annotation(ru.t1.java.demo.aop.LogDataSourceError)")
    public void anyDataSourceErrorLogAnnotatedMethod() {
    }

    @Pointcut("execution(* ru.t1.java.demo.service.*.*(..))")
    public void anyServiceMethod() {
    }

    @AfterThrowing(pointcut = """
            beanAnnotatedWithRepository()
            || anyCrudRepositoryMethod()
            || anyProxyImplementingCrudRepository()
            || anyDataSourceErrorLogAnnotatedMethod()
            """, throwing = "exception")
    public void logException(JoinPoint joinPoint, Exception exception) throws Exception {
        try {
            log.error("Произошла ошибка при работе с БД, см. в dataSourceErrorLog");
            String message = StringUtils.truncate(ObjectUtils.firstNonNull(exception.getMessage(),
                    exception.getLocalizedMessage()), 500);
            saveDatasourceErrorLog(joinPoint, exception, message);
        } catch (Exception e) {
            exception.addSuppressed(e);
            throw exception;
        }
        throw exception;
    }

    @AfterThrowing(pointcut = "anyServiceMethod()", throwing = "entityNotFoundException")
    public void logException(JoinPoint joinPoint, JpaNotFoundException entityNotFoundException) {
        try {
            log.error("Произошла ошибка поиска сущности, см. в dataSourceErrorLog");
            String message = StringUtils.truncate(ObjectUtils.defaultIfNull(
                    entityNotFoundException.getMessage(), NOT_FOUND_MESSAGE), 500);
            saveDatasourceErrorLog(joinPoint, entityNotFoundException, message);
        } catch (Exception e) {
            entityNotFoundException.addSuppressed(e);
            throw new RuntimeException(entityNotFoundException);
        }
        throw new JpaException(entityNotFoundException.getMessage());
    }

    private void saveDatasourceErrorLog(JoinPoint joinPoint, Exception exception, String message) {
        String stacktrace = StringUtils.truncate(StringUtils.join(exception.getStackTrace(), LINE_BREAK), 1000);
        var datasourceException = DataSourceErrorLog.builder()
                .message(message)
                .stacktrace(stacktrace)
                .methodSignature(StringUtils.truncate(joinPoint.getSignature().toString(), 255))
                .build();
        dataSourceErrorLogRepository.save(datasourceException);
    }
}
