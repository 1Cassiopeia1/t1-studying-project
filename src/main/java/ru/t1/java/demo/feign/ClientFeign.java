package ru.t1.java.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.t1.java.demo.dto.TransactionAcceptDto;

@FeignClient
public interface ClientFeign {
    @PostMapping("/accept")
    ResponseEntity<Void> saveEvent(@RequestBody TransactionAcceptDto acceptDto);

    @GetMapping("/accept")
    ResponseEntity<Boolean> isClientBlocked(@RequestParam Long accountId, @RequestParam Long clientId);
}
