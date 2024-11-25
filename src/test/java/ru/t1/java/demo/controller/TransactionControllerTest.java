package ru.t1.java.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.service.TransactionService;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser()
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void getTransactionTest() throws Exception {
        mockMvc.perform(get("/transaction/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void postTransactionTest() throws Exception {
        TransactionDto transactionDto = TransactionDto.builder()
                .accountId(1L)
                .amount("100.00")
                .executionTime(LocalTime.now())
                .transactionStatus(TransactionStatus.ACCEPTED)
                .timestamp(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDto)))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).saveTransaction(any(TransactionDto.class));
    }

    @Test
    void postMockedTransactionTest() throws Exception {
        mockMvc.perform(post("/transactions/mock")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).saveMockedTransactions();
    }

    @Test
    void updateTransactionTest() throws Exception {
        Long transactionId = 1L;
        TransactionDto transactionDto = TransactionDto.builder()
                .accountId(1L)
                .amount("150.00")
                .executionTime(LocalTime.now())
                .transactionStatus(TransactionStatus.BLOCKED)
                .timestamp(LocalDateTime.now())
                .build();

        mockMvc.perform(put("/transaction/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDto)))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).updateTransaction(any(TransactionDto.class), eq(transactionId));
    }

    @Test
    void deleteTransactionTest() throws Exception {
        Long transactionId = 1L;

        mockMvc.perform(delete("/transaction/{id}", transactionId))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).deleteTransaction(transactionId);
    }
}
