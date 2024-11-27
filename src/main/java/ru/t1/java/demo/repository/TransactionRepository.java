package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByTimestamp(LocalDateTime timestamp);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Transaction t set t.transactionStatus = :transactionStatus where t.transactionId = :transactionId")
    void updateStatusById(TransactionStatus transactionStatus, Long transactionId);

    @Query("""
            select COUNT(t) from Transaction t
            join t.account ac join ac.client
            where ac.client.clientId = :clientId
            and t.transactionStatus = 'REJECTED'
            """)
    long countRejectedTransactionsByClientId(Long clientId);
}
