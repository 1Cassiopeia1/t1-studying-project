package ru.t1.java.demo.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.ToStringSerializer;
import org.springframework.util.backoff.FixedBackOff;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.kafka.KafkaProducer;

import java.util.Map;

@Slf4j
@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "t1")
public class KafkaConfig<T> {

    @Value("${t1.kafka.session-timeout-ms:15000}")
    private String sessionTimeout;
    @Value("${t1.kafka.max-partition-fetch-bytes:300000}")
    private String maxPartitionFetchBytes;
    @Value("${t1.kafka.max-poll-interval-ms:3000}")
    private String maxPollIntervalsMs;
    @Value("${t1.kafka.topic.client_id_registered}")
    private String clientTopic;
    @NestedConfigurationProperty
    private KafkaProperties kafka = new KafkaProperties();

    @Bean
    public KafkaAdmin commonKafkaAdmin(ObjectProvider<SslBundles> sslBundles) {
        var adminProperties = this.kafka.buildAdminProperties(sslBundles.getIfAvailable());
        return new KafkaAdmin(adminProperties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ClientDto> clientKafkaListenerContainerFactory(
            ObjectProvider<SslBundles> sslBundles, RecordMessageConverter recordMessageConverter) {
        Map<String, Object> consumerProperties = kafka.buildConsumerProperties(sslBundles.getIfAvailable());
        consumerProperties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        consumerProperties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);
        consumerProperties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalsMs);
        ConsumerFactory<String, ClientDto> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties);
        ConcurrentKafkaListenerContainerFactory<String, ClientDto> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        fillContainerFactory(recordMessageConverter, containerFactory, consumerFactory);
        containerFactory.setCommonErrorHandler(errorHandler());
        return containerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AccountDto> accountKafkaListenerContainerFactory(
            ObjectProvider<SslBundles> sslBundles, RecordMessageConverter recordMessageConverter) {
        Map<String, Object> consumerProperties = kafka.buildConsumerProperties(sslBundles.getIfAvailable());
        ConsumerFactory<String, AccountDto> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties);
        ConcurrentKafkaListenerContainerFactory<String, AccountDto> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        fillContainerFactory(recordMessageConverter, containerFactory, consumerFactory);
        return containerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionDto> transactionKafkaListenerContainerFactory(
            ObjectProvider<SslBundles> sslBundles, RecordMessageConverter recordMessageConverter) {
        Map<String, Object> consumerProperties = kafka.buildConsumerProperties(sslBundles.getIfAvailable());
        ConsumerFactory<String, TransactionDto> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties);
        ConcurrentKafkaListenerContainerFactory<String, TransactionDto> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        fillContainerFactory(recordMessageConverter, containerFactory, consumerFactory);
        return containerFactory;
    }

    @Bean
    public KafkaTemplate<String, T> kafkaTemplate(ObjectProvider<SslBundles> sslBundles) {
        Map<String, Object> props = getProducerProperties(sslBundles);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ToStringSerializer.class);
        DefaultKafkaProducerFactory<String, T> producerFactory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, T> kafkaJsonTemplate(ObjectProvider<SslBundles> sslBundles) {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(getProducerProperties(sslBundles)));
    }

    @Bean
    @ConditionalOnProperty(value = "t1.kafka.producer.enable", havingValue = "true", matchIfMissing = true)
    public KafkaProducer<T> producerClient(@Qualifier("kafkaJsonTemplate") KafkaTemplate<String, T> template) {
        template.setDefaultTopic(clientTopic);
        return new KafkaProducer<>(template);
    }

    private <R> void fillContainerFactory(RecordMessageConverter recordMessageConverter,
                                          ConcurrentKafkaListenerContainerFactory<String, R> containerFactory,
                                          ConsumerFactory<String, R> consumerFactory) {
        containerFactory.setRecordMessageConverter(recordMessageConverter);
        containerFactory.setConsumerFactory(consumerFactory);
        containerFactory.setConcurrency(kafka.getListener().getConcurrency());
        containerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        containerFactory.getContainerProperties().setSyncCommits(true);
        containerFactory.getContainerProperties().setMicrometerEnabled(true);
    }

    private Map<String, Object> getProducerProperties(ObjectProvider<SslBundles> sslBundles) {
        Map<String, Object> props = kafka.buildProducerProperties(sslBundles.getIfAvailable());
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return props;
    }

    private CommonErrorHandler errorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(new FixedBackOff(1000, 3));
        handler.addNotRetryableExceptions(IllegalStateException.class);
        handler.setRetryListeners((consumerRecord, ex, deliveryAttempt) ->
                log.error(" RetryListeners message = {}, offset = {} deliveryAttempt = {}", ex.getMessage(), consumerRecord.offset(), deliveryAttempt));
        return handler;
    }
}
