package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.model.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("update Account a set a.balance = :newBalance where a.accountId = :accountId")
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void updateBalance(Long accountId, String newBalance);

    @Query("from Account a join fetch a.client where a.accountId = :accountId")
    Optional<Account> findByIdWithClient(Long accountId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Account a set a.accountStatus = 'BLOCKED' where a.accountId = :accountId")
    void setBloked(Long accountId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Account a set a.accountStatus = 'ARRESTED' where a.accountId = :accountId")
    void setArrested(Long accountId);
}
