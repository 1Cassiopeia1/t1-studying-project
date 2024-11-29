package ru.t1.java.demo.kafka;

import com.example.t1projectspringbootstarter.config.KafkaConfig;
import com.example.t1projectspringbootstarter.dto.TransactionDto;
import lombok.Cleanup;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import ru.t1.java.demo.service.TransactionService;

import static org.mockito.ArgumentMatchers.any;
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
        var producerRecord = new ProducerRecord<String, TransactionDto>(topic, 0, null, new TransactionDto());
        producer.send(producerRecord);

        verify(transactionService, timeout(defaultVerifyTimeout.toMillis())).handleTransaction(any(TransactionDto.class));
    }
}
