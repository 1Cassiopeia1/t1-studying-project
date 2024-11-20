package ru.t1.java.demo.aop;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.MetricDto;
import ru.t1.java.demo.kafka.KafkaProducer;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(1)
public class MetricAspect {

    private static final String HEADER_ERROR_NAME = "Error-Name";

    private final KafkaProducer<MetricDto> kafkaProducer;
    private final MeterRegistry meterRegistry;
    @Value("${t1.kafka.topic.t1_demo_metrics}")
    private String topicName;

    @Around("@annotation(ru.t1.java.demo.aop.Metric) || within(@ru.t1.java.demo.aop.Metric *)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        var metricAnnotation = AnnotationUtils.findAnnotation(method, Metric.class);
        if (metricAnnotation == null) {
            metricAnnotation = AnnotationUtils.findAnnotation(joinPoint.getSignature().getDeclaringType(), Metric.class);
        }
        if (metricAnnotation == null) {
            log.error("No metric annotation found for {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }

        // Запоминаем время начала выполнения
        long startTime = System.currentTimeMillis();

        Object result;
        try {
            // Выполняем метод
            result = joinPoint.proceed();
        } finally {
            // Вычисляем время выполнения
            long executionTime = System.currentTimeMillis() - startTime;

            // Проверяем, превышает ли время выполнения заданное значение
            if (executionTime > metricAnnotation.maxValue()) {
                // Формируем сообщение для отправки в Kafka
                String methodName = joinPoint.getSignature().getName();
                String parameters = joinPoint.getArgs() != null
                        ? String.join(", ", Arrays.toString(joinPoint.getArgs()))
                        : null;

                MetricDto metricDto = new MetricDto(methodName, parameters, executionTime);
                List<Header> headers = List.of(new RecordHeader(HEADER_ERROR_NAME, "METRICS".getBytes(StandardCharsets.UTF_8)));
                syncKafkaSend(metricDto, headers);
                meterRegistry.counter("execution-time-overtime").increment();
            }
        }

        return result;
    }

    private void syncKafkaSend(MetricDto metricDto, List<Header> headers) throws InterruptedException, ExecutionException {
        kafkaProducer.sendTo(topicName, metricDto, headers)
                .thenAccept(sendResult -> log.info("Message sent successfully: {}", metricDto))
                .handle((sendResult, t) -> {
                    if (t != null) {
                        log.error("Failed to send message: {}", t.getMessage());
                        return CompletableFuture.failedFuture(t);
                    }
                    return sendResult;
                }).get();
    }
}