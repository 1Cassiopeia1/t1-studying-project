package ru.t1.java.demo.service;

import ru.t1.java.demo.dto.AccountDto;

public interface AccountService {
    AccountDto getAccount(Long accId);

    void saveAccount(AccountDto accountDto);

    void saveMockedAccounts();

    void updateAccount(AccountDto account, Long accId);

    void deleteAccount(Long accId);
}
