package ru.t1.java.demo.service;

import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.Transaction;

import java.util.List;

public interface TransactionService {
    TransactionDto getTransaction(Long tId);

    void saveTransaction(TransactionDto transactionDto);

    void saveTransactionEntity(Transaction transaction);

    void saveMockedTransactions();

    void updateTransaction(TransactionDto transactionDto, Long id);

    void deleteTransaction(Long id);

    void saveAllTransactions(List<TransactionDto> transactions);

}
