package ru.t1.java.demo.mappers;

import org.mapstruct.Mapper;
import ru.t1.java.demo.config.MapstructConfig;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.Account;

import java.util.List;

@Mapper(config = MapstructConfig.class)
public interface AccountMapper {
    Account fromDtoToEntity(AccountDto accountDto);

    List<Account> fromDtoToEntity(List<AccountDto> accounts);

    AccountDto fromEntityToDto(Account account);
}
