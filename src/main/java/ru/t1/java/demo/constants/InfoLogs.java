package ru.t1.java.demo.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InfoLogs {
    public static final String TRANSACTION_SAVED = "[STUD-I001] Transaction saved successfully";
    public static final String ACCOUNT_SAVED_SUCCESSFULLY = "[STUD-I002] Account saved successfully";
    public static final String ACCOUNT_AND_TRANSACTION_BLOCKED =
            "[STUD-I003] Счет с ID {} заблокирован, сумма заблокированных транзакций: {}";
    public static final String TRANSACTION_STATUS_UPDATED = "[STUD-I004] Статус транзакции с ID {} обновлен на {}";
}
