package ru.t1.java.demo.service;

import java.util.List;

public interface MockService {
    <T> List<T> getMockData(String json, Class<T> clazz);
}
