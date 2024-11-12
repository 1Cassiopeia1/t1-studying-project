package ru.t1.java.demo.mappers;

import org.mapstruct.Mapper;
import ru.t1.java.demo.config.MapstructConfig;
import ru.t1.java.demo.dto.TransactionAcceptDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;

@Mapper(config = MapstructConfig.class)
public interface TransactionAcceptMapper {
TransactionAcceptDto toDtoForAcceptation(Account account, Transaction transaction);
}
