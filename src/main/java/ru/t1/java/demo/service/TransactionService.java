package ru.t1.java.demo.service;

import ru.t1.java.demo.dto.TransactionDto;

public interface TransactionService {
    TransactionDto getTransaction(Long tId);

    void saveTransaction(TransactionDto transactionDto);

    void saveMockedTransactions();

    void updateTransaction(TransactionDto transactionDto, Long id);

    void deleteTransaction(Long id);
}
