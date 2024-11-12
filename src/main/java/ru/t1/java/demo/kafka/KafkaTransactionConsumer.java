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
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.dto.TransactionAcceptDto;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.mappers.AccountMapper;
import ru.t1.java.demo.mappers.TransactionAcceptMapper;
import ru.t1.java.demo.mappers.TransactionMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.AccountType;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.TransactionService;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTransactionConsumer {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final TransactionMapper transactionMapper;
    private final KafkaProducer<TransactionAcceptDto> kafkaProducer;
    private final AccountMapper accountMapper;
    private final TransactionAcceptMapper transactionAcceptMapper;

    @RetryableTopic(
            attempts = "1",
            kafkaTemplate = "kafkaTemplate",
            dltStrategy = DltStrategy.FAIL_ON_ERROR)
    //TODO изменить groupId на второй сервис
    @KafkaListener(groupId = "${t1.kafka.consumer.group-id}",
            topics = "${t1.kafka.topic.t1_demo_transactions}",
            containerFactory = "transactionKafkaListenerContainerFactory")
    @Transactional
    public void handleTransaction(@Payload TransactionDto transactionDto,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                  Acknowledgment ack) {
        transactionProcessing(transactionDto, topic, key, ack);
    }

    @DltHandler
    public void handleTransactionsDlt(@Payload TransactionDto transactionDto,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                      Acknowledgment ack) {
        transactionProcessing(transactionDto, topic, key, ack);
    }

    private void transactionProcessing(@Payload TransactionDto transactionDto,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                       @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                       Acknowledgment ack) {
        try {
            Optional<Account> optionalAccount = Optional.ofNullable(accountService.getAccountEntity(transactionDto.getAccountId()));

            optionalAccount.filter(account -> AccountStatus.OPEN.equals(account.getAccountStatus()))
                    .ifPresentOrElse(account -> {
                        Transaction transaction = processTransaction(transactionDto, account);
                        kafkaProducer.send(transactionAcceptMapper.toDtoForAcceptation(account, transaction));
                    }, () -> {
                        log.error("Во время обработки сообщения в топике {} возникла ошибка, статус счета не OPEN", topic);
                    });
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения в топике {}: {}", topic, e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }

        log.debug("Сообщение в topic {} с ключом {} обработано", topic, key);
    }

    private Transaction processTransaction(TransactionDto transactionDto, Account account) {
        Transaction transaction = transactionMapper.fromDtoToEntity(transactionDto);
        transaction.setTransactionStatus(TransactionStatus.REQUESTED);
        transactionService.saveTransactionEntity(transaction);

        BigDecimal transactionAmount = new BigDecimal(transaction.getAmount());
        BigDecimal newBalance = calculateNewBalance(account, transactionAmount);
        account.setBalance(newBalance.toString());

        accountService.updateAccount(accountMapper.fromEntityToDto(account), account.getAccountId());

        return transaction;
    }

    private BigDecimal calculateNewBalance(Account account, BigDecimal transactionAmount) {
        BigDecimal currentBalance = new BigDecimal(account.getBalance());
        if (AccountType.CREDIT.equals(account.getAccountType())) {
            return currentBalance.add(transactionAmount);
        } else {
            return currentBalance.subtract(transactionAmount);
        }
    }
}
