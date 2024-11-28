package ru.t1.java.demo.mappers;

import com.example.t1projectspringbootstarter.config.MapstructConfig;
import com.example.t1projectspringbootstarter.dto.AccountDto;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.t1.java.demo.model.Account;

import java.util.List;

@Mapper(config = MapstructConfig.class)
public interface AccountMapper {
    @Mapping(target = "client.clientId", source = "clientId")
    Account fromDtoToEntity(AccountDto accountDto);

    List<Account> fromDtoToEntity(List<AccountDto> accounts);

    @InheritInverseConfiguration
    AccountDto fromEntityToDto(Account account);
}
