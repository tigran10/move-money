package com.movemoney.storage;


import com.movemoney.ex.MoveMoneyExceptionWithCode;

public class StorageException extends MoveMoneyExceptionWithCode {

    public StorageException(ExeptionErrorCode code) {
        super(code);
    }

    public StorageException(String message, Throwable cause, ExeptionErrorCode code) {
        super(message, cause, code);
    }

    public StorageException(String message, ExeptionErrorCode code) {
        super(message, code);
    }

    public StorageException(Throwable cause, ExeptionErrorCode code) {
        super(cause, code);
    }
}
