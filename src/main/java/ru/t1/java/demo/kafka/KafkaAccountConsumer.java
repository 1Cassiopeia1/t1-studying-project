package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.service.AccountService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(value = "t1.kafka.enabled", havingValue = "true")
public class KafkaAccountConsumer {

    private final AccountService accountService;

    @RetryableTopic(
            attempts = "1",
            kafkaTemplate = "kafkaTemplate",
            dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(groupId = "${t1.kafka.consumer.group-id}",
            topics = "${t1.kafka.topic.t1_demo_accounts}",
            containerFactory = "accountKafkaListenerContainerFactory")
    public void receiveAccounts(@Payload List<AccountDto> accountList,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                Acknowledgment ack) {
        handleMessage(accountList, topic, key, ack);
    }

    @DltHandler
    public void receiveAccountsDlt(@Payload List<AccountDto> accountList,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                   Acknowledgment ack) {
        handleMessage(accountList, topic, key, ack);
    }

    private void handleMessage(List<AccountDto> accountList, String topic, String key, Acknowledgment ack) {
        log.debug("Получено сообщение в topic {} с ключом {}", topic, key);

        try {
            accountService.saveAllAccounts(accountList);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Во время обработки сообщения в топике {} возникла ошибка", topic, e);
            throw e;
        }

        log.debug("Сообщение в topic {} с ключом {} обработано", topic, key);
    }
}
