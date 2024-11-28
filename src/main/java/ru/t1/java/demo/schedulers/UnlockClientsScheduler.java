package ru.t1.java.demo.schedulers;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.constants.InfoLogs;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.ClientService;

@RequiredArgsConstructor
@Component
@Slf4j
@ConditionalOnProperty(name = "scheduler.unlock-clients.enable", havingValue = "true")
public class UnlockClientsScheduler {

    private final ClientService clientService;
    private final MeterRegistry meterRegistry;
    private final ClientRepository clientRepository;
    @Value("${scheduler.unlock-clients.amount}")
    private Integer clientAmount;

    @Scheduled(cron = "${scheduler.unlock-clients.cron}", scheduler = "clientTaskExecutor")
    @SchedulerLock(name = "unlock-client-sync", lockAtLeastFor = "20s")
    public void unlockClients() {
        log.info(InfoLogs.UNLOCK_CLIENT_SCHEDULER_START);
        meterRegistry.gauge("blocked_clients", clientRepository.countAllBlocked());
        clientService.unlock(clientAmount);
        meterRegistry.gauge("blocked_clients", clientRepository.countAllBlocked());
        log.info(InfoLogs.UNLOCK_CLIENT_SCHEDULER_FINISHED);
    }
}
