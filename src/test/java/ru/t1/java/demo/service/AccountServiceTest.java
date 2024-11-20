package ru.t1.java.demo.service;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.t1.java.demo.config.TestContainersConfig;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@DisplayName("Тесты AccountService")
class AccountServiceTest implements TestContainersConfig {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AccountRepository repository;
    @Autowired
    private AccountService accountService;
    @MockBean
    private MockService mockService;
    @Autowired
    private DataSourceErrorLogRepository dataSourceErrorLogRepository;
    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void setUp() {
        // Очищаем базу данных перед каждым тестом
        JdbcTestUtils.deleteFromTables(jdbcClient, "transaction", "account", "client", "data_source_error_log");
    }

    @Test
    @DisplayName("Проверяет отработку аспекта на аннотированном методе")
    void saveMockedAccountsParseJsonTest() {
        when(mockService.getMockData(any(), any())).thenThrow(new RuntimeException("json parse fail"));

        // When
        assertThrows(RuntimeException.class, () -> accountService.saveMockedAccounts());

        // Then
        List<DataSourceErrorLog> dataSourceErrorLogs = dataSourceErrorLogRepository.findAll();
        assertEquals(1, dataSourceErrorLogs.size());
        assertNotNull(dataSourceErrorLogs.get(0).getStacktrace());
        assertEquals("void ru.t1.java.demo.service.impl.AccountServiceImpl.saveMockedAccounts()",
                dataSourceErrorLogs.get(0).getMethodSignature());
        assertEquals("json parse fail", dataSourceErrorLogs.get(0).getMessage());
    }

    @Test
    void saveAccountTest() {
        // Given
        AccountDto accountDto = Instancio.of(AccountDto.class).create();
        Client client = Instancio.of(Client.class).create();
        var savedClient = clientRepository.save(client);
        accountDto.setClientId(savedClient.getClientId());

        // When
        accountService.saveAccount(accountDto);

        // Then
        List<Account> result = repository.findAll();
        assertNotNull(result);
        assertEquals(result.get(0).getClientId(), accountDto.getClientId());

    }

    @Test
    void getAccountTest() {
        // Given
        Account account = Instancio.of(Account.class).create();
        Client client = Instancio.of(Client.class).create();
        var savedClient = clientRepository.save(client);
        account.setClientId(savedClient.getClientId());
        var savedAccount = repository.save(account);

        // When
        AccountDto accountDto = accountService.getAccount(savedAccount.getAccountId());

        // Then
        assertNotNull(accountDto);
        assertEquals(account.getClientId(), accountDto.getClientId());
        assertEquals(account.getAccountType(), accountDto.getAccountType());
        assertEquals(account.getBalance(), accountDto.getBalance());

    }

    @Test
    void deleteAccountTest() {
        Account account = Instancio.of(Account.class).create();
        Client client = Instancio.of(Client.class).create();
        var savedClient = clientRepository.save(client);
        account.setClientId(savedClient.getClientId());
        var savedAccount = repository.save(account);

        accountService.deleteAccount(savedAccount.getAccountId());

        assertFalse(repository.existsById(savedAccount.getAccountId()));
    }

    @Test
    void updateAccountTest() {
        Account oldAccount = Instancio.of(Account.class).create();
        Client client = Instancio.of(Client.class).create();
        var savedClient = clientRepository.save(client);
        oldAccount.setClientId(savedClient.getClientId());
        var savedAccount = repository.save(oldAccount);
        AccountDto updatingAccountDto = Instancio.of(AccountDto.class).create();
        updatingAccountDto.setClientId(oldAccount.getClientId());

        accountService.updateAccount(updatingAccountDto, savedAccount.getAccountId());

        Account updatedAccount = repository.findById(savedAccount.getAccountId()).orElseThrow();
        assertEquals(updatingAccountDto.getClientId(), updatedAccount.getClientId());
        assertEquals(updatingAccountDto.getAccountType(), updatedAccount.getAccountType());
        assertEquals(updatingAccountDto.getBalance(), updatedAccount.getBalance());
    }

    @Test
    void testSaveMockedAccounts() {
        String mockedAccountsResource = resourceLoader.getResource("classpath:mocked_accounts.json").toString();
        List<AccountDto> expectedAccountDtos = mockService.getMockData(mockedAccountsResource, AccountDto.class);

        String mockedClientsResource = resourceLoader.getResource("classpath:MOCK_DATA.json").toString();
        List<Client> expectedClients = mockService.getMockData(mockedClientsResource, Client.class);

        // When
        accountService.saveMockedAccounts();

        // Then
        List<Account> savedAccounts = repository.findAll();
        assertEquals(expectedAccountDtos.size(), savedAccounts.size());

        for (int i = 0; i < savedAccounts.size(); i++) {
            Account savedAccount = savedAccounts.get(i);
            AccountDto expectedAccountDto = expectedAccountDtos.get(i);
            Client expectedClient = expectedClients.get(i);
            assertEquals(expectedAccountDto.getAccountType(), savedAccount.getAccountType());
            assertEquals(expectedAccountDto.getBalance(), savedAccount.getBalance());
            assertEquals(expectedAccountDto.getClientId(), savedAccount.getClientId());

            // Проверяем связь с клиентом
            assertEquals(expectedClient.getClientId(), savedAccount.getClientId());
        }
    }
}
