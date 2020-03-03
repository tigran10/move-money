package com.movemoney.storage;

public enum ExeptionErrorCode {
    POSSIBLE_MESSAGE_REPLY_ERROR(-1),
    DUPLICATE_ENTRY(001),
    RESOURCE_NOT_FOUND(002),

    INSUFFICIENT_AMOUNT(101),
    UNSUCCESSFUL_ROLLBACK(201),
    USER_ERROR(003);

    public final int code;

    private ExeptionErrorCode(int code) {
        this.code = code;
    }
}