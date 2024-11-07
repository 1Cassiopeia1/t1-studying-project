package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.kafka.KafkaProducer;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.util.ClientMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repository;
    private final KafkaProducer kafkaProducer;

    @PostConstruct
    void init() {
        List<Client> clients;
        try {
            clients = parseJson();
        } catch (IOException e) {
            log.error("Ошибка во время обработки записей", e);
            throw new RuntimeException(e);
        }
        if (repository.count() == 0) {
            repository.saveAll(clients);
        }
    }

    @Override
    public List<Client> parseJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ClientDto[] clients = mapper.readValue(new File("src/main/resources/MOCK_DATA.json"), ClientDto[].class);

        return Arrays.stream(clients)
                .map(ClientMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Client> registerClients(List<Client> clients) {
        List<Client> savedClients = new ArrayList<>();

        for (Client client : clients) {
            // Сохраняем клиента в репозитории
            Client saved = repository.save(client);
            savedClients.add(saved);
            // Отправляем сообщение в Kafka
            kafkaProducer.send(client);
        }

        return savedClients;
    }

    @Override
    public Client registerClient(Client client) {
        Client saved = repository.save(client);
        kafkaProducer.send(client);
        return saved;
    }
}
