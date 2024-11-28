package ru.t1.java.demo.mappers;

import com.example.t1projectspringbootstarter.config.MapstructConfig;
import com.example.t1projectspringbootstarter.dto.TransactionDto;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.t1.java.demo.model.Transaction;

import java.util.List;

@Mapper(config = MapstructConfig.class)
public interface TransactionMapper {
    @Mapping(target = "account.accountId", source = "accountId")
    Transaction fromDtoToEntity(TransactionDto transactionDto);

    List<Transaction> fromDtoToEntity(List<TransactionDto> transactionDto);

    @InheritInverseConfiguration
    TransactionDto fromEntityToDto(Transaction transaction);
}
