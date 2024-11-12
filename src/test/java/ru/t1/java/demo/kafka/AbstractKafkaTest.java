package ru.t1.java.demo.kafka;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.t1.java.demo.config.CommonKafkaConfig;

import java.time.Duration;
import java.util.List;

@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(classes = {
        KafkaAutoConfiguration.class,
        CommonKafkaConfig.class,
        JacksonAutoConfiguration.class})
abstract class AbstractKafkaTest {

    @Autowired
    protected EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    protected ObjectMapper objectMapper;

    @Value("${test.verify-timeout}")
    protected Duration defaultVerifyTimeout;

    protected <T> Producer<String, List<T>> createProducer(Class<T> clazz) {
        var producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker.getBrokersAsString());
        var keySerializer = new StringSerializer();
        JavaType listJavaType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
        Serializer<List<T>> valueSerializer = new JsonSerializer<>(listJavaType, objectMapper);
        var producerFactory = new DefaultKafkaProducerFactory<>(producerProps, keySerializer, valueSerializer, false);
        return producerFactory.createProducer();
    }
}
