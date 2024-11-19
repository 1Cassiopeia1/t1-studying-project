package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link ru.t1.java.demo.kafka.KafkaTransactionConsumer}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionAcceptDto implements Serializable {
    private Long clientId;
    private Long accountId;
    private Long transactionId;
    private LocalDateTime timestamp;
    private String transactionAmount;
    private String accountBalance;
}