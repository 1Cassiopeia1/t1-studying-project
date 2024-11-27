package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.constants.ErrorLogs;
import ru.t1.java.demo.constants.InfoLogs;
import ru.t1.java.demo.dto.ResultDto;
import ru.t1.java.demo.dto.TransactionAcceptDto;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.exception.DbEntryNotFoundException;
import ru.t1.java.demo.kafka.KafkaProducer;
import ru.t1.java.demo.mappers.TransactionAcceptMapper;
import ru.t1.java.demo.mappers.TransactionMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.MockService;
import ru.t1.java.demo.service.TransactionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private static final String MODELS_MOCKED_TRANSACTIONS_JSON_PATH = "models/mocked_transactions.json";
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final MockService mockService;
    private final AccountService accountService;
    private final KafkaProducer<TransactionAcceptDto> kafkaTransactionAcceptProducer;
    private final TransactionAcceptMapper transactionAcceptMapper;
    private final TransactionTemplate transactionTemplate;
    @Value("${t1.kafka.topic.t1_demo_transaction_accept}")
    private String acceptTopic;

    @Override
    @LogDataSourceError
    public TransactionDto getTransaction(Long tId) {
        Transaction transaction = transactionRepository.findById(tId)
                .orElseThrow(DbEntryNotFoundException::new);
        return transactionMapper.fromEntityToDto(transaction);
    }

    @Override
    @LogDataSourceError
    public void saveTransaction(TransactionDto transactionDto) {
        transactionRepository.save(transactionMapper.fromDtoToEntity(transactionDto));
        log.info(InfoLogs.TRANSACTION_SAVED);
    }

    @Override
    public void saveTransactionEntity(Transaction transaction) {
        transactionRepository.save(transaction);
        log.info(InfoLogs.TRANSACTION_SAVED);
    }

    // метод падает, потому что падает на constrain на fk на базу - попадает в advice
    @Override
    @LogDataSourceError
    public void saveMockedTransactions() {
        List<TransactionDto> transactions = mockService.getMockData(MODELS_MOCKED_TRANSACTIONS_JSON_PATH, TransactionDto.class);
        transactionRepository.saveAll(transactionMapper.fromDtoToEntity(transactions));
    }

    @Override
    @Transactional
    @LogDataSourceError
    public void updateTransaction(TransactionDto transactionDto, Long transactionId) {
        Transaction newTransaction = transactionMapper.fromDtoToEntity(transactionDto);
        Transaction oldTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(DbEntryNotFoundException::new);
        copyProperties(newTransaction, oldTransaction);
    }

    @Override
    @LogDataSourceError
    public void deleteTransaction(Long id) {

        transactionRepository.deleteById(id);
    }

    @Override
    public void handleTransaction(TransactionDto transactionDto) {
        // проверка для идемпотентности обработки ответа и семантики exactly once
        if (!transactionRepository.existsByTimestamp(transactionDto.getTimestamp())) {
            Transaction transaction = transactionRepository.save(transactionMapper.fromDtoToEntity(transactionDto));
            Optional.of(transaction)
                    .map(trans -> accountService.getAccountEntity(transactionDto.getAccountId()))
                    .filter(account -> AccountStatus.OPEN.equals(account.getAccountStatus()))
                    .ifPresentOrElse(account -> {
                        // TODO добавить транзакции на кафку и идемпотентность
                        // на будущее можно подумать о транзакции на кафку в этом месте, объединяя сохранение в базу
                        changeBalance(transaction.getTransactionId(), transactionDto.getAmount(), account);
                        kafkaTransactionAcceptProducer.sendTo(
                                acceptTopic,
                                transactionAcceptMapper.toDtoForAcceptation(account, transaction),
                                null);
                    }, () -> log.warn(ErrorLogs.HANDLE_TRANSACTION_NOT_ACCEPTED, transactionDto.getAccountId()));
        }
    }

    @Override
    public void handleTransactionAcceptationResponse(ResultDto resultDto) {
        transactionRepository.findById(resultDto.getTransactionId())
                .ifPresentOrElse(transaction -> {
                    switch (resultDto.getTransactionStatus()) {
                        case ACCEPTED -> updateTransactionStatus(transaction.getTransactionId(), TransactionStatus.ACCEPTED);
                        case BLOCKED -> transactionTemplate.executeWithoutResult(transactionStatus -> {
                            updateTransactionStatus(transaction.getTransactionId(), TransactionStatus.BLOCKED);
                            BigDecimal frozenAmount = new BigDecimal(transaction.getAmount());
                            accountService.handleBlockedBalance(resultDto.getAccountId(), frozenAmount);
                        });
                        case REJECTED -> transactionTemplate.executeWithoutResult(transactionStatus -> {
                            updateTransactionStatus(transaction.getTransactionId(), TransactionStatus.REJECTED);
                            BigDecimal transactionAmount = new BigDecimal(transaction.getAmount());
                            accountService.updateBalance(resultDto.getAccountId(), transactionAmount.negate());
                        });
                        default -> throw new NotImplementedException("Неизвестный статус транзакции: %s"
                                .formatted(resultDto.getTransactionStatus()));
                    }
                }, () -> log.error(ErrorLogs.TRANSACTION_NOT_FOUND, resultDto.getTransactionId()));
    }

    // лок на транзакцию не ставим, допускаем параллельную смену статусов из разных потоков
    private void updateTransactionStatus(Long transactionId, TransactionStatus newStatus) {
        transactionRepository.updateStatusById(newStatus, transactionId);
        log.info(InfoLogs.TRANSACTION_STATUS_UPDATED, transactionId, newStatus);
    }

    private void changeBalance(Long transactionId, String amount, Account account) {
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            updateTransactionStatus(transactionId, TransactionStatus.REQUESTED);
            BigDecimal transactionAmount = new BigDecimal(amount);
            accountService.updateBalance(account.getAccountId(), transactionAmount);
        });
    }

    private void copyProperties(Transaction source, Transaction target) {
        BeanUtils.copyProperties(source, target, "transactionId");
    }

}
