package ru.t1.java.demo.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.kafka.KafkaProducer;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class LogDataSourceErrorAspect {

    private static final String LINE_BREAK = "\n";
    private static final String HEADER_ERROR_NAME = "Error-Name";

    private final DataSourceErrorLogRepository dataSourceErrorLogRepository;
    private final KafkaProducer<String> kafkaProducer;
    @Value("${t1.kafka.topic.t1_demo_metrics}")
    private String topicName;


    @Pointcut("@annotation(ru.t1.java.demo.aop.LogDataSourceError)")
    public void anyDataSourceErrorLogAnnotatedMethod() {
    }

    @AfterThrowing(pointcut = "anyDataSourceErrorLogAnnotatedMethod()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Exception exception) throws Exception {
        try {
            log.error("Произошла ошибка при работе с БД, см. в dataSourceErrorLog");
            String message = StringUtils.truncate(ObjectUtils.firstNonNull(exception.getMessage(),
                    exception.getLocalizedMessage()), 500);
            messageToKafka(joinPoint, exception, message);
        } catch (Exception e) {
            exception.addSuppressed(e);
        } finally {
            throw exception;
        }
    }

    private void messageToKafka(JoinPoint joinPoint, Exception exception,
                                String message) throws ExecutionException, InterruptedException {
        List<Header> headers = List.of(new RecordHeader(HEADER_ERROR_NAME, "DATA_SOURCE".getBytes(StandardCharsets.UTF_8)));
        kafkaProducer.sendTo(topicName, message, headers)
                .thenAccept(sendResult -> log.info("Message sent successfully: " + message))
                .handle((sendResult, t) -> {
                    if (t != null) {
                        saveDatasourceErrorLog(joinPoint, exception, message);
                        return CompletableFuture.failedFuture(t);
                    }
                    return sendResult;
                }).get();
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
