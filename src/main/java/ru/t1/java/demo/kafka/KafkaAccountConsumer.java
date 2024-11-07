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
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.mappers.AccountMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.service.AccountService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaAccountConsumer {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @KafkaListener(groupId = "${t1.kafka.consumer.group-id}",
            topics = "${t1.kafka.topic.t1_demo_accounts}",
            containerFactory = "accountKafkaListenerContainerFactory")
    public void receiveAccounts(@Payload AccountDto accountList,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                Acknowledgment ack) {
        log.debug("Получено сообщение в topic {} с ключом {}", topic, key);

        try {
            List<Account> accounts = List.of(accountList).stream()
                    .map(accountMapper::fromDtoToEntity)
                    .toList();
            accountService.saveAllAccounts(accounts);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Во время обработки сообщения в топике {} возникла ошибка", topic, e);
        }

        log.debug("Сообщение в topic {} с ключом {} обработано", topic, key);
    }
}
