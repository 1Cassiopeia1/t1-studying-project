package ru.t1.java.demo.service;

import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    Account getAccountEntity(Long accId);

    AccountDto getAccount(Long accId);

    void saveAccount(AccountDto accountDto);

    void saveMockedAccounts();

    void updateAccount(AccountDto account, Long accId);

    void updateBalance(Long accountId, BigDecimal amount);

    void deleteAccount(Long accId);

    void saveAllAccounts(List<AccountDto> accounts);

    void saveAccountEntity(Account account);

    void handleBlockedBalance(Long accountId, BigDecimal frozenAmount);
}
