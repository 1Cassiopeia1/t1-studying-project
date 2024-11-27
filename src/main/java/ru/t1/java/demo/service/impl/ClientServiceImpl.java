package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.feign.ClientFeign;
import ru.t1.java.demo.kafka.KafkaProducer;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.util.ClientMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaProducer<Client> kafkaProducer;
    private final ClientFeign clientFeign;
    private final TransactionTemplate transactionTemplate;

    @PostConstruct
    void init() {
        List<Client> clients;
        try {
            clients = parseJson();
        } catch (IOException e) {
            log.error("Ошибка во время обработки записей", e);
            throw new RuntimeException(e);
        }
        if (clientRepository.count() == 0) {
            clientRepository.saveAll(clients);
        }
    }

    @Override
    public List<Client> parseJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ClientDto[] clients = mapper.readValue(new File("src/main/resources/models/MOCK_DATA.json"), ClientDto[].class);

        return Arrays.stream(clients)
                .map(ClientMapper::toEntity)
                .toList();
    }

    @Override
    public List<Client> registerClients(List<Client> clients) {
        List<Client> savedClients = new ArrayList<>();

        for (Client client : clients) {
            // Сохраняем клиента в репозитории
            Client saved = clientRepository.save(client);
            savedClients.add(saved);
            // Отправляем сообщение в Kafka
            kafkaProducer.send(client);
        }

        return savedClients;
    }

    @Override
    public Client registerClient(Client client) {
        Client saved = clientRepository.save(client);
        kafkaProducer.send(client);
        return saved;
    }

    @Override
    public boolean checkBlockedAndSetRejected(Long clientId, Long accountId, Long transactionId) {
        return Optional.of(clientFeign.isClientAccountsBlocked(clientId, accountId))
                .map(HttpEntity::getBody)
                .filter(Predicate.isEqual(true))
                .map(blocked -> {
                    transactionTemplate.executeWithoutResult(transactionStatus -> {
                        clientRepository.setBloked(clientId);
                        accountRepository.setBloked(accountId);
                        transactionRepository.updateStatusById(TransactionStatus.REJECTED, transactionId);
                    });
                    return blocked;
                }).orElse(false);
    }
}
