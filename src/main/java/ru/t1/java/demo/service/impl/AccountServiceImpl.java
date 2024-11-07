package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.exception.DbEntryNotFoundException;
import ru.t1.java.demo.kafka.KafkaProducer;
import ru.t1.java.demo.mappers.AccountMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.MockService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final MockService mockService;
    private final KafkaProducer<Account> kafkaAccountProducer;

    @Override
    @LogDataSourceError
    public AccountDto getAccount(Long accId) {
        Account account = accountRepository.findById(accId)
                .orElseThrow(DbEntryNotFoundException::new);
        return accountMapper.fromEntityToDto(account);
    }

    @Override
    @LogDataSourceError
    public void saveAccount(AccountDto accountDto) {
        accountRepository.save(accountMapper.fromDtoToEntity(accountDto));
        log.info("Account saved successfully");
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
        List<AccountDto> accountDtos = mockService.getMockData("mocked_accounts.json", AccountDto.class);
        List<Client> clients = mockService.getMockData("MOCK_DATA.json", Client.class);
        List<Account> accounts = accountMapper.fromDtoToEntity(accountDtos);

        if (clients.size() != accountDtos.size()) {
            throw new IllegalStateException();
        }
        for (int i = 0; i < clients.size(); i++) {
            accounts.get(i).setClientId(clients.get(i).getId());
        }
        accountRepository.saveAll(accounts);
    }

    private void copyProperties(Account source, Account target) {
        BeanUtils.copyProperties(source, target, "id");
    }

    @Override
    public void saveAllAccounts(List<Account> accounts) {
        accountRepository.saveAll(accounts);
    }
}
