package ru.t1.java.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.service.TransactionService;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping(value = "/transaction/{id}")
    @Operation(description = "Получение транзакции по id")
    public TransactionDto getTransaction(@PathVariable Long id) {
        return transactionService.getTransaction(id);
    }

    @PostMapping(value = "/transaction")
    @Operation(description = "Сохранение моканых данных по транзакциям, намерянно не доработан для тестирования аспекта")
    public void postAccount(@RequestBody TransactionDto transactionDto) {
        transactionService.saveTransaction(transactionDto);
    }

    @PostMapping(value = "/transactions/mock")
    @Operation(description = "Сохранение новой транзакции")
    public void postMockedAccount() {
        transactionService.saveMockedTransactions();
    }

    @PutMapping(value = "/transaction/{id}")
    @Operation(description = "Изменение транзакции обновлёнными данными")
    public void updateAccount(@RequestBody TransactionDto transactionDto, @PathVariable Long id) {
        transactionService.updateTransaction(transactionDto, id);
    }

    @DeleteMapping(value = "/transaction/{id}")
    @Operation(description = "Удаление транзакции по id, удаляет только корневую сущность")
    public void deleteAccount(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }
}
