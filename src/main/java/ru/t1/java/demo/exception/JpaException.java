package ru.t1.java.demo.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class JpaException extends RuntimeException {
    public JpaException(String message) {
        super(message);
    }
}
