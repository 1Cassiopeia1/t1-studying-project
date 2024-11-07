package ru.t1.java.demo.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.MetricDto;
import ru.t1.java.demo.kafka.KafkaProducer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MetricAspect {

    private final KafkaProducer<MetricDto> kafkaProducer;
    @Value("${t1.kafka.topic.t1_demo_metrics}")
    private String topicName;
    private static final String HEADER_ERROR_NAME = "Error-Name";


    @Around("@annotation(metric) || within(@ru.t1.java.demo.aop.Metric *)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint, Metric metric) throws Throwable {
        long startTime = System.currentTimeMillis(); // Запоминаем время начала выполнения

        Object result;
        try {
            result = joinPoint.proceed(); // Выполняем метод
        } finally {
            long executionTime = System.currentTimeMillis() - startTime; // Вычисляем время выполнения

            // Проверяем, превышает ли время выполнения заданное значение
            if (executionTime > metric.maxValue()) {
                // Формируем сообщение для отправки в Kafka
                String methodName = joinPoint.getSignature().getName();
                String parameters = joinPoint.getArgs() != null ? String.join(", " +
                        "", (CharSequence[]) joinPoint.getArgs()) : "No parameters";
                MetricDto message = new MetricDto(methodName, parameters, executionTime);

                List<Header> headers = List.of(new RecordHeader(HEADER_ERROR_NAME, "METRICS".getBytes(StandardCharsets.UTF_8)));
                kafkaProducer.sendTo(topicName, message, headers)
                        .thenAccept(sendResult -> log.info("Message sent successfully: " + message))
                        .handle((sendResult, t) -> {
                            if (t != null) {
                                log.error("Failed to send message: " + t.getMessage());
                                return CompletableFuture.failedFuture(t);
                            }
                            return sendResult;
                        }).get();
            }
        }

        return result;
    }
}