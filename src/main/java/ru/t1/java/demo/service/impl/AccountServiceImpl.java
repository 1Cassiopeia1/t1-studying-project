package ru.t1.java.demo.service.impl;

import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.constants.InfoLogs;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.exception.DbEntryNotFoundException;
import ru.t1.java.demo.mappers.AccountMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.AccountType;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.MockService;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private static final String MODELS_MOCKED_ACCOUNTS_JSON_PATH = "models/mocked_accounts.json";
    private static final String MODELS_MOCK_DATA_JSON_PATH = "models/MOCK_DATA.json";
    private static final String DEFAULT_ZERO_VALUE = "0";

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final MockService mockService;

    @Override
    @LogDataSourceError
    public AccountDto getAccount(Long accId) {
        Account account = accountRepository.findById(accId)
                .orElseThrow(DbEntryNotFoundException::new);
        return accountMapper.fromEntityToDto(account);
    }

    @Override
    public Account getAccountEntity(Long accId) {
        return accountRepository.findById(accId)
                .orElseThrow(DbEntryNotFoundException::new);
    }

    @Override
    @LogDataSourceError
    public void saveAccount(AccountDto accountDto) {
        accountRepository.save(accountMapper.fromDtoToEntity(accountDto));
        log.info(InfoLogs.ACCOUNT_SAVED_SUCCESSFULLY);
    }

    @Override
    @Transactional
    @LogDataSourceError
    public void updateAccount(AccountDto account, Long accId) {
        Account newAccount = accountMapper.fromDtoToEntity(account);
        Account oldAccount = accountRepository.findById(accId)
                .orElseThrow(DbEntryNotFoundException::new);
        copyProperties(newAccount, oldAccount);
    }

    @Override
    @LogDataSourceError
    public void deleteAccount(Long accId) {
        accountRepository.deleteById(accId);
    }

    // падает в advice, даже если не распарсился json, так как стоит @LogDataSourceError
    @Override
    @LogDataSourceError
    public void saveMockedAccounts() {
        List<AccountDto> accountDtos = mockService.getMockData(MODELS_MOCKED_ACCOUNTS_JSON_PATH, AccountDto.class);
        List<Client> clients = mockService.getMockData(MODELS_MOCK_DATA_JSON_PATH, Client.class);
        List<Account> accounts = accountMapper.fromDtoToEntity(accountDtos);

        if (clients.size() != accountDtos.size()) {
            throw new IllegalStateException();
        }
        for (int i = 0; i < clients.size(); i++) {
            accounts.get(i).setClientId(clients.get(i).getClientId());
        }
        accountRepository.saveAll(accounts);
    }

    @Override
    public void saveAllAccounts(List<AccountDto> accountDtos) {
        List<Account> accounts = accountDtos.stream()
                .map(accountMapper::fromDtoToEntity)
                .toList();
        accountRepository.saveAll(accounts);
    }

    @Override
    public void saveAccountEntity(Account account) {
        accountRepository.save(account);
    }

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public void updateBalance(Long accountId, BigDecimal amount) {
        accountRepository.findById(accountId)
                .map(currentAccount -> calculateNewBalance(currentAccount, amount))
                .ifPresent(newBalance -> accountRepository.updateBalance(accountId, newBalance.toString()));
    }

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public void handleBlockedBalance(Long accountId, BigDecimal frozenAmount) {
        accountRepository.findById(accountId).ifPresent(account -> {
                    account.setAccountStatus(AccountStatus.BLOCKED);

                    // Корректируем баланс и устанавливаем frozenAmount
                    BigDecimal newBalance = calculateNewBalance(account, frozenAmount.negate());
                    account.setBalance(newBalance.toString());
                    account.setFrozenAmount(frozenAmount.toString());

                    log.info(InfoLogs.ACCOUNT_AND_TRANSACTION_BLOCKED, account.getAccountId(), frozenAmount);
                }
        );
    }

    private BigDecimal calculateNewBalance(Account account, BigDecimal transactionAmount) {
        // WARNING уточнить у аналитика корректность такого вычисления currentBalance
        BigDecimal currentBalance = new BigDecimal(ObjectUtils.defaultIfNull(account.getBalance(), DEFAULT_ZERO_VALUE));
        if (AccountType.CREDIT.equals(account.getAccountType())) {
            return currentBalance.subtract(transactionAmount);
        } else {
            return currentBalance.add(transactionAmount);
        }
    }

    private void copyProperties(Account source, Account target) {
        BeanUtils.copyProperties(source, target, "accountId");
    }
}
