package ru.t1.java.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.t1.java.demo.config.TestContainersConfig;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.AccountType;
import ru.t1.java.demo.service.impl.AccountServiceImpl;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerTest implements TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountServiceImpl accountService;

    @Test
    void getAccountTest() throws Exception {
        Long accountId = 1L;
        AccountDto accountDto = getAccountDto();

        when(accountService.getAccount(accountId)).thenReturn(accountDto);

        mockMvc.perform(get("/account/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(1L))
                .andExpect(jsonPath("$.accountType").value("CREDIT"))
                .andExpect(jsonPath("$.balance").value("1000"))
                .andExpect(jsonPath("$.accountStatus").value("OPEN"))
                .andExpect(jsonPath("$.frozenAmount").value("0"));

        verify(accountService).getAccount(accountId);
    }

    @Test
    void testPostMockedAccounts() throws Exception {
        doNothing().when(accountService).saveMockedAccounts();

        mockMvc.perform(post("/accounts/mock"))
                .andExpect(status().isOk());

        verify(accountService).saveMockedAccounts();
    }

    @Test
    void testPostAccount() throws Exception {
        AccountDto accountDto = getAccountDto();

        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isOk());

        verify(accountService).saveAccount(any(AccountDto.class));
    }

    @Test
    void testUpdateAccount() throws Exception {
        Long accountId = 1L;
        AccountDto accountDto = getAccountDto();

        mockMvc.perform(put("/account/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isOk());

        verify(accountService).updateAccount(any(AccountDto.class), eq(accountId));
    }

    @Test
    void testDeleteAccount() throws Exception {
        Long accountId = 1L;
        doNothing().when(accountService).deleteAccount(accountId);

        mockMvc.perform(delete("/account/{id}", accountId))
                .andExpect(status().isOk());

        verify(accountService).deleteAccount(accountId);
    }

    private static AccountDto getAccountDto() {
        return new AccountDto()
                .setClientId(1L)
                .setAccountType(AccountType.CREDIT)
                .setBalance("1000")
                .setAccountStatus(AccountStatus.OPEN)
                .setFrozenAmount("0");
    }
}
