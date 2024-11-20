package ru.t1.java.demo.kafka;

import lombok.Cleanup;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import ru.t1.java.demo.config.KafkaConfig;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.service.TransactionService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@DisplayName("Тесты консьюмеров транзакций")
@EmbeddedKafka(
        partitions = 1,
        topics = "${t1.kafka.topic.t1_demo_transactions}",
        bootstrapServersProperty = "${t1.kafka.bootstrap-servers}")
@Import({KafkaConfig.class,
        KafkaTransactionConsumer.class})
class TransactionConsumerTest extends AbstractKafkaTest {
    @Value("${t1.kafka.topic.t1_demo_transactions}")
    private String topic;

    @MockBean
    private TransactionService transactionService;

    @Test
    void receive() {
        @Cleanup var producer = createProducer(TransactionDto.class);
        List<TransactionDto> transactionDtos = List.of(new TransactionDto());

        var producerRecord = new ProducerRecord<String, List<TransactionDto>>(topic, 0, null, transactionDtos);
        producer.send(producerRecord);

        verify(transactionService, timeout(defaultVerifyTimeout.toMillis())).saveAllTransactions(anyList());
    }
}
