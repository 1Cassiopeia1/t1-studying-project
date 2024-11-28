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
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.service.AccountService;

@RequiredArgsConstructor
@Component
@Slf4j
@ConditionalOnProperty(name = "scheduler.unlock-accounts.enable", havingValue = "true")
public class UnlockAccountsScheduler {

    private final AccountService accountService;
    private final MeterRegistry meterRegistry;
    private final AccountRepository accountRepository;
    @Value("${scheduler.unlock-accounts.amount}")
    private Integer accountAmount;

    @Scheduled(cron = "${scheduler.unlock-accounts.cron}", scheduler = "accountTaskExecutor")
    @SchedulerLock(name = "unlock-account-sync", lockAtLeastFor = "20s")
    public void unlockAccounts() {
        log.info(InfoLogs.UNLOCK_ACCOUNT_SCHEDULER_START);
        meterRegistry.gauge("arrested_accounts", accountRepository.countAllArrested());
        accountService.unlock(accountAmount);
        meterRegistry.gauge("arrested_accounts", accountRepository.countAllArrested());
        log.info(InfoLogs.UNLOCK_ACCOUNT_SCHEDULER_FINISHED);
    }
}
