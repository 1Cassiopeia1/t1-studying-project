package ru.t1.java.demo.service;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.t1.java.demo.config.TestContainersConfig;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.exception.JpaException;
import ru.t1.java.demo.exception.JpaNotFoundException;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;
import ru.t1.java.demo.repository.TransactionRepository;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class TransactionServiceTest implements TestContainersConfig {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private DataSourceErrorLogRepository dataSourceErrorLogRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcClient, "transaction", "account", "client", "data_source_error_log");
    }

    @Test
    void saveTransactionTest() {
        TransactionDto transactionDto = Instancio.of(TransactionDto.class).create();
        transactionDto.setExecutionTime(LocalTime.now().minusMinutes(5));
        Account account = Instancio.of(Account.class).create();
        Client client = Instancio.of(Client.class).create();
        var savedClient = clientRepository.save(client);
        account.setClientId(savedClient.getId());
        var savedAccount = accountRepository.save(account);
        transactionDto.setAccountId(savedAccount.getId());

        // When
        transactionService.saveTransaction(transactionDto);

        //Then
        List<Transaction> result = transactionRepository.findAll();
        assertNotNull(result);
        assertEquals(result.get(0).getAccountId(), transactionDto.getAccountId());

    }

    @Test
    void getTransactionTest() {
        Transaction transaction = Instancio.of(Transaction.class).create();
        transaction.setExecutionTime(LocalTime.now());
        var transactionId = saveTransaction(transaction).getId();

        // When
        TransactionDto transactionDto = transactionService.getTransaction(transactionId);

        // Then
        assertNotNull(transactionDto);
        assertEquals(transaction.getAccountId(), transactionDto.getAccountId());
        assertEquals(transaction.getAmount(), transactionDto.getAmount());
        assertThat(transaction.getExecutionTime()).isCloseTo(transactionDto.getExecutionTime(),
                within(1, ChronoUnit.MILLIS));
    }

    @Test
    void getTransactionExceptionTest() {
        // When
        JpaException exception = assertThrows(JpaException.class,
                () -> transactionService.getTransaction(1L));

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
        List<DataSourceErrorLog> dataSourceErrorLogs = dataSourceErrorLogRepository.findAll();
        assertEquals(1, dataSourceErrorLogs.size());
        assertNotNull(dataSourceErrorLogs.get(0).getStacktrace());
        assertEquals("TransactionDto ru.t1.java.demo.service.impl.TransactionServiceImpl.getTransaction(Long)",
                dataSourceErrorLogs.get(0).getMethodSignature());
        assertEquals("Сущность не найдена по указанным параметрам", dataSourceErrorLogs.get(0).getMessage());
    }

    @Test
    void deleteTransactionTest() {
        Transaction transaction = new Transaction();
        var transactionId = saveTransaction(transaction).getId();

        // When
        transactionService.deleteTransaction(transactionId);

        // Then
        assertFalse(transactionRepository.existsById(transactionId));
    }

    @Test
    void updateTransactionTest() {
        Transaction oldTransaction = Instancio.of(Transaction.class).create();
        oldTransaction.setExecutionTime(LocalTime.now().minusMinutes(5));
        var savedTransaction = saveTransaction(oldTransaction);
        TransactionDto updatingTransactionDto = Instancio.of(TransactionDto.class).create();
        updatingTransactionDto.setExecutionTime(LocalTime.now());
        updatingTransactionDto.setAccountId(oldTransaction.getAccountId());

        // When
        transactionService.updateTransaction(updatingTransactionDto, savedTransaction.getId());

        // Then
        Transaction updatedTransaction = transactionRepository.findById(savedTransaction.getId())
                .orElseThrow(JpaNotFoundException::new);
        assertEquals(updatingTransactionDto.getAccountId(), updatedTransaction.getAccountId());
        assertEquals(updatingTransactionDto.getAmount(), updatedTransaction.getAmount());
        assertThat(updatingTransactionDto.getExecutionTime()).isCloseTo(updatedTransaction.getExecutionTime(),
                within(1, ChronoUnit.MILLIS));
    }

    @Test
    void updateTransactionNotFoundTest() {
        Long transactionId = 100L;
        TransactionDto updatedTransactionDto = Instancio.of(TransactionDto.class).create();
        // When / Then
        assertThrows(JpaException.class,
                () -> transactionService.updateTransaction(updatedTransactionDto, transactionId));
        List<DataSourceErrorLog> dataSourceErrorLogs = dataSourceErrorLogRepository.findAll();
        assertEquals(1, dataSourceErrorLogs.size());
        assertNotNull(dataSourceErrorLogs.get(0).getStacktrace());
        assertEquals("void ru.t1.java.demo.service.impl.TransactionServiceImpl.updateTransaction(TransactionDto,Long)",
                dataSourceErrorLogs.get(0).getMethodSignature());
        assertEquals("Сущность не найдена по указанным параметрам", dataSourceErrorLogs.get(0).getMessage());
    }

    private Transaction saveTransaction(Transaction oldTransaction) {
        Account account = Instancio.of(Account.class).create();
        Client client = Instancio.of(Client.class).create();
        var savedClient = clientRepository.save(client);
        account.setClientId(savedClient.getId());
        var savedAccount = accountRepository.save(account);
        oldTransaction.setAccountId(savedAccount.getId());
        return transactionRepository.save(oldTransaction);
    }
}