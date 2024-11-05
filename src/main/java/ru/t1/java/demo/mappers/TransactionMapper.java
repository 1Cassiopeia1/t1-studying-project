package ru.t1.java.demo.mappers;

import org.mapstruct.Mapper;
import ru.t1.java.demo.config.MapstructConfig;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.Transaction;

import java.util.List;

@Mapper(config = MapstructConfig.class)
public interface TransactionMapper {
    Transaction fromDtoToEntity(TransactionDto transactionDto);

    List<Transaction> fromDtoToEntity(List<TransactionDto> transactionDto);

    TransactionDto fromEntityToDto(Transaction transaction);
}
