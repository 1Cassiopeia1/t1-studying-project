package ru.t1.java.demo.controller;

import com.example.t1projectspringbootstarter.dto.TransactionDto;
import com.example.t1projectspringbootstarter.dto.enums.TransactionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.t1.java.demo.config.TestContainersConfig;
import ru.t1.java.demo.service.TransactionService;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerTest implements TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    void getTransactionTest() throws Exception {
        mockMvc.perform(get("/transaction/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void postTransactionTest() throws Exception {
        TransactionDto transactionDto = getTransactionDto();

        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDto)))
                .andExpect(status().isOk());

        verify(transactionService).saveTransaction(any(TransactionDto.class));
    }

    @Test
    void postMockedTransactionTest() throws Exception {
        mockMvc.perform(post("/transactions/mock")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(transactionService).saveMockedTransactions();
    }

    @Test
    void updateTransactionTest() throws Exception {
        Long transactionId = 1L;
        TransactionDto transactionDto = getTransactionDto();

        mockMvc.perform(put("/transaction/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDto)))
                .andExpect(status().isOk());

        verify(transactionService).updateTransaction(any(TransactionDto.class), eq(transactionId));
    }

    @Test
    void deleteTransactionTest() throws Exception {
        Long transactionId = 1L;

        mockMvc.perform(delete("/transaction/{id}", transactionId))
                .andExpect(status().isOk());

        verify(transactionService).deleteTransaction(transactionId);
    }

    private static TransactionDto getTransactionDto() {
        return TransactionDto.builder()
                .accountId(1L)
                .amount("100.00")
                .executionTime(LocalTime.now())
                .transactionStatus(TransactionStatus.ACCEPTED)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
