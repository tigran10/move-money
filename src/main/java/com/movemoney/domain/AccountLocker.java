package com.movemoney.domain;

public interface AccountLocker {
    void lock(Account.Id accountId);

    void unlock(Account.Id accountId);
}
