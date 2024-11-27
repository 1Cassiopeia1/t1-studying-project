package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.t1.java.demo.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("update Account a set a.balance = :newBalance where a.accountId = :accountId")
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void updateBalance(Long accountId, String newBalance);
}
