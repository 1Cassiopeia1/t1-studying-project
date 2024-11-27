package ru.t1.java.demo.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.t1.java.demo.config.MapstructConfig;
import ru.t1.java.demo.dto.TransactionAcceptDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;

@Mapper(config = MapstructConfig.class)
public interface TransactionAcceptMapper {
    @Mapping(target = "accountId", source = "transaction.account.accountId")
    @Mapping(target = "accountBalance", source = "account.balance")
    @Mapping(target = "transactionAmount", source = "transaction.amount")
    @Mapping(target = "clientId", source = "account.client.clientId")
    TransactionAcceptDto toDtoForAcceptation(Account account, Transaction transaction);
}
