package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.service.MockService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockServiceImpl implements MockService {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public <T> List<T> getMockData(String jsonPath, Class<T> elementClass) {
        try {
            URL url = Optional.ofNullable(MockService.class.getClassLoader().getResource(jsonPath))
                    .orElseThrow(() -> new FileNotFoundException(jsonPath));
            CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass);
            return objectMapper.readValue(url, type);
        } catch (IOException e) {
            log.error("Ошибка во время обработки записей", e);
            throw e;
        }
    }
}
