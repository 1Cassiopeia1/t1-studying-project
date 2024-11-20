package ru.t1.java.demo.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DbEntryNotFoundException extends JpaException {

    public DbEntryNotFoundException(String message) {
        super(message);
    }
}
