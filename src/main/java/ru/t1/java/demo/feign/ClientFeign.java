package ru.t1.java.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.t1.java.demo.config.FeignClientConfiguration;
import ru.t1.java.demo.dto.TransactionAcceptDto;

@FeignClient(name = "accept-service",
        url = "${services.client-service.url}",
        configuration = FeignClientConfiguration.class)
public interface ClientFeign {
    @PostMapping("/accept")
    ResponseEntity<Void> saveEvent(@RequestBody TransactionAcceptDto acceptDto);

    @GetMapping("/block")
    ResponseEntity<Boolean> isClientAccountsBlocked(@RequestParam Long clientId, @RequestParam Long accountId);
}
