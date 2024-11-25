package ru.t1.java.demo.controller;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.AccountType;
import ru.t1.java.demo.service.impl.AccountServiceImpl;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser()
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountServiceImpl accountService;

    @InjectMocks
    private AccountController accountController;


//    @Configuration
//    @EnableWebSecurity
//    static class TestSecurityConfig extends WebSecurityConfigurerAdapter {
//        protected void configure(HttpSecurity http) throws Exception {
//            http.csrf().disable() // Отключаем CSRF для тестов
//                    .authorizeHttpRequests(authorizeHttpRequestsCustomizer)
//                    .anyRequest().permitAll(); // Разрешаем все запросы
//        }
//    }

    @Test
    void getAccountTest() throws Exception {
        Long accountId = 1L;
        AccountDto accountDto = new AccountDto()
                .setClientId(1L)
                .setAccountType(AccountType.CREDIT)
                .setBalance("1000")
                .setAccountStatus(AccountStatus.OPEN)
                .setFrozenAmount("0");

        when(accountService.getAccount(accountId)).thenReturn(accountDto);

        mockMvc.perform(get("/account/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(1L))
                .andExpect(jsonPath("$.accountType").value("CREDIT"))
                .andExpect(jsonPath("$.balance").value("1000"))
                .andExpect(jsonPath("$.accountStatus").value("OPEN"))
                .andExpect(jsonPath("$.frozenAmount").value("0"));

        verify(accountService, times(1)).getAccount(accountId);
    }

    @Test
    void testPostMockedAccounts() throws Exception {
        doNothing().when(accountService).saveMockedAccounts();

        mockMvc.perform(post("/accounts/mock"))
                .andExpect(status().isOk());

        verify(accountService, times(1)).saveMockedAccounts();
    }

    @Test
    void testPostAccount() throws Exception {
        AccountDto accountDto = new AccountDto()
                .setClientId(1L)
                .setAccountType(AccountType.DEBIT)
                .setBalance("1000")
                .setAccountStatus(AccountStatus.OPEN)
                .setFrozenAmount("0");

        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientId\":1,\"accountType\":\"DEBIT\"," +
                                "\"balance\":\"1000\",\"accountStatus\":\"OPEN\"," +
                                "\"frozenAmount\":\"0\"}"))
                .andExpect(status().isOk());

        verify(accountService, times(1)).saveAccount(any(AccountDto.class));
    }

    @Test
    void testUpdateAccount() throws Exception {
        Long accountId = 1L;
        AccountDto accountDto = new AccountDto()
                .setClientId(1L)
                .setAccountType(AccountType.CREDIT)
                .setBalance("1500")
                .setAccountStatus(AccountStatus.OPEN)
                .setFrozenAmount("0");

        mockMvc.perform(put("/account/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientId\":1,\"accountType\":\"CREDIT\"," +
                                "\"balance\":\"150\",\"accountStatus\":\"OPEN\"," +
                                "\"frozenAmount\":\"0\"}"))
                .andExpect(status().isOk());

        verify(accountService, times(1)).updateAccount(any(AccountDto.class), eq(accountId));
    }

    @Test
    void testDeleteAccount() throws Exception {
        Long accountId = 1L;
        doNothing().when(accountService).deleteAccount(accountId);

        mockMvc.perform(delete("/account/{id}", accountId))
                .andExpect(status().isOk());

        verify(accountService, times(1)).deleteAccount(accountId);
    }
}
