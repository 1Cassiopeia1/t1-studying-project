package ru.t1.java.demo.controller;

import com.example.t1projectspringbootstarter.aop.Metric;
import com.example.t1projectspringbootstarter.dto.AccountDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.java.demo.service.AccountService;

@RestController
@RequiredArgsConstructor
@Metric(maxValue = 5000L)
public class AccountController {

    private final AccountService accountService;

    @GetMapping(value = "/account/{id}")
    @Operation(description = "Получение аккаунта по id")
    public AccountDto getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    @PostMapping(value = "/accounts/mock")
    @Operation(description = "Создание моков на базе")
    public void postMockedAccounts() {
        accountService.saveMockedAccounts();
    }

    @PostMapping(value = "/account")
    @Operation(description = "Сохранение нового аккаунта")
    public void postAccount(@RequestBody AccountDto accountDto) {
        accountService.saveAccount(accountDto);
    }

    @PutMapping(value = "/account/{id}")
    @Operation(description = "Обновление аккаунта новыми данными")
    public void updateAccount(@RequestBody AccountDto accountDto, @PathVariable Long id) {
        accountService.updateAccount(accountDto, id);
    }

    @DeleteMapping(value = "/account/{id}")
    @Operation(description = "Удаление аккаунта по id")
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }
}
