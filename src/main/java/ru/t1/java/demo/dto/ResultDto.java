package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.io.Serializable;

/**
 * DTO for {@link ru.t1.java.demo.kafka.KafkaTransactionResultConsumer}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultDto implements Serializable {
    private TransactionStatus transactionStatus;
    private Long accountId;
    private Long transactionId;
}