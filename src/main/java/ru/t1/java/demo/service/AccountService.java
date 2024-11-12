package ru.t1.java.demo.service;

import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.Account;

import java.util.List;

public interface AccountService {
    AccountDto getAccount(Long accId);

    void saveAccount(AccountDto accountDto);

    void saveMockedAccounts();

    void updateAccount(AccountDto account, Long accId);

    void deleteAccount(Long accId);

    void saveAllAccounts(List<AccountDto> accounts);

}
