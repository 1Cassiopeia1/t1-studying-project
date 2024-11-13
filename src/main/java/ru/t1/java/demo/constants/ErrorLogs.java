package ru.t1.java.demo.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorLogs {
    public static final String HANDLE_TRANSACTION_NOT_ACCEPTED =
            "[STUD-W001] При получении транзакции по счёту с id {} статус счета не OPEN или транзакция уже обработана";
}
