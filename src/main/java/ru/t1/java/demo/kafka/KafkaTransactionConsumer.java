package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.mappers.TransactionMapper;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.service.TransactionService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTransactionConsumer {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @KafkaListener(groupId = "${t1.kafka.consumer.group-id}",
            topics = "${t1.kafka.topic.t1_demo_transactions}",
            containerFactory = "transactionKafkaListenerContainerFactory")
    public void receiveTransactions(@Payload List<TransactionDto> transactionList,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                    Acknowledgment ack) {
        log.debug("Получено сообщение в topic {} с ключом {}", topic, key);

        try {
            List<Transaction> transactions = transactionList.stream()
                    .map(transactionMapper::fromDtoToEntity)
                    .toList();
            transactionService.saveAllTransactions(transactions);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Во время обработки сообщения в топике {} возникла ошибка", topic, e);
        }

        log.debug("Сообщение в topic {} с ключом {} обработано", topic, key);
    }
}
