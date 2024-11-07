package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.exception.DbEntryNotFoundException;
import ru.t1.java.demo.mappers.TransactionMapper;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.MockService;
import ru.t1.java.demo.service.TransactionService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository repository;
    private final TransactionMapper transactionMapper;
    private final MockService mockService;

    @Override
    @LogDataSourceError
    public TransactionDto getTransaction(Long tId) {
        Transaction transaction = repository.findById(tId)
                .orElseThrow(DbEntryNotFoundException::new);
        return transactionMapper.fromEntityToDto(transaction);
    }

    @Override
    @LogDataSourceError
    public void saveTransaction(TransactionDto transactionDto) {
        repository.save(transactionMapper.fromDtoToEntity(transactionDto));
        log.info("Transaction saved successfully");
    }

    // метод падает, потому что падает на constraint на fk на базу - попадает в advice
    @Override
    @LogDataSourceError
    public void saveMockedTransactions() {
        List<TransactionDto> transactions = mockService.getMockData("mocked_transactions.json", TransactionDto.class);
        repository.saveAll(transactionMapper.fromDtoToEntity(transactions));
    }

    @Override
    @Transactional
    @LogDataSourceError
    public void updateTransaction(TransactionDto transactionDto, Long transactionId) {
        Transaction newTransaction = transactionMapper.fromDtoToEntity(transactionDto);
        Transaction oldTransaction = repository.findById(transactionId)
                .orElseThrow(DbEntryNotFoundException::new);
        copyProperties(newTransaction, oldTransaction);
    }

    @Override
    @LogDataSourceError
    public void deleteTransaction(Long id) {

        repository.deleteById(id);
    }

    private void copyProperties(Transaction source, Transaction target) {
        BeanUtils.copyProperties(source, target, "id");
    }

    @Override
    public void saveAllTransactions(List<Transaction> transactions) {
        repository.saveAll(transactions);
    }
}
