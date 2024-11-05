package ru.t1.java.demo.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class JpaNotFoundException extends JpaException {

    public JpaNotFoundException(String message) {
        super(message);
    }
}
