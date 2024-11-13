package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.ResultDto;
import ru.t1.java.demo.service.TransactionService;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTransactionResultConsumer {

    private final TransactionService transactionService;

    @RetryableTopic(
            attempts = "1",
            kafkaTemplate = "kafkaTemplate",
            dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(groupId = "${t1.kafka.consumer.group-id}",
            topics = "${t1.kafka.topic.t1_demo_transaction_result}",
            containerFactory = "accountKafkaListenerContainerFactory")
    public void receiveTransactionResult(@Payload ResultDto resultDto,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                         @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                         Acknowledgment ack) {
        handleResult(resultDto, topic, key, ack);
    }

    @DltHandler
    public void receiveTransactionResultDlt(@Payload ResultDto resultDto,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                            Acknowledgment ack) {
        handleResult(resultDto, topic, key, ack);
    }

    private void handleResult(@Payload ResultDto resultDto,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                              @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                              Acknowledgment ack) {
        try {
            transactionService.handleResult(resultDto);
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения в топике {}: {}", topic, e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }
}
