package ru.t1.java.demo.service;

import com.example.t1projectspringbootstarter.dto.TransactionDto;
import ru.t1.java.demo.dto.ResultDto;
import ru.t1.java.demo.model.Transaction;

public interface TransactionService {
    TransactionDto getTransaction(Long tId);

    void saveTransaction(TransactionDto transactionDto);

    void saveTransactionEntity(Transaction transaction);

    void saveMockedTransactions();

    void updateTransaction(TransactionDto transactionDto, Long id);

    void deleteTransaction(Long id);

    void handleTransaction(TransactionDto transactionDto);

    void handleTransactionAcceptationResponse(ResultDto resultDto);
}
