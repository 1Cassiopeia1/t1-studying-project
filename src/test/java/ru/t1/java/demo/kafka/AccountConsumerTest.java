package ru.t1.java.demo.kafka;

import com.example.t1projectspringbootstarter.config.KafkaConfig;
import com.example.t1projectspringbootstarter.dto.AccountDto;
import lombok.Cleanup;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import ru.t1.java.demo.service.AccountService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@DisplayName("Тесты консьюмеров аккаунтов")
@EmbeddedKafka(
        partitions = 1,
        topics = "${t1.kafka.topic.t1_demo_accounts}",
        bootstrapServersProperty = "${t1.kafka.bootstrap-servers}")
@Import({KafkaConfig.class,
        KafkaAccountConsumer.class})
class AccountConsumerTest extends AbstractKafkaTest {
    @Value("${t1.kafka.topic.t1_demo_accounts}")
    private String topic;

    @MockBean
    private AccountService accountService;

    @Test
    void receive() {
        @Cleanup var producer = createListProducer(AccountDto.class);
        List<AccountDto> accountDto = List.of(new AccountDto());

        var producerRecord = new ProducerRecord<String, List<AccountDto>>(topic, 0, null, accountDto);
        producer.send(producerRecord);

        verify(accountService, timeout(defaultVerifyTimeout.toMillis())).saveAllAccounts(anyList());
    }
}
