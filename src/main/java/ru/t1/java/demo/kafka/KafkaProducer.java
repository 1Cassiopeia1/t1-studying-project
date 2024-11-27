package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Component
@Primary
public class KafkaProducer<T> {

    private final KafkaTemplate<String, T> kafkaJsonTemplate;
    @Value("${t1.kafka.enabled}")
    private boolean isKafkaEnabled;

    public void send(T o) {
        if (isKafkaEnabled) {
            try {
                kafkaJsonTemplate.sendDefault(UUID.randomUUID().toString(), o).get();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            } finally {
                kafkaJsonTemplate.flush();
            }
        }
    }

    public CompletableFuture<SendResult<String, T>> sendTo(String topic, T value, List<Header> headers) {
        if (isKafkaEnabled) {
            try {
                ProducerRecord<String, T> message = new ProducerRecord<>(topic, null, UUID.randomUUID().toString(), value, headers);
                return kafkaJsonTemplate.send(message);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return CompletableFuture.failedFuture(ex);
            } finally {
                kafkaJsonTemplate.flush();
            }
        } else {
            return CompletableFuture.completedFuture(new SendResult<>(null, null));
        }
    }
}
