package ru.t1.java.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.java.demo.aop.Metric;
import ru.t1.java.demo.exception.ClientException;
import ru.t1.java.demo.service.ClientService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @GetMapping(value = "/client")
    @Operation(description = "Спит 3 секунды и бросает исключение")
    @Metric(maxValue = 3000L)
    public void doSomething() {
        try {
            clientService.parseJson();
            Thread.sleep(3000L);
            throw new ClientException();
        } catch (Exception e) {
            log.info("Catching exception from ClientController");
            throw new ClientException();
        }
    }
}
