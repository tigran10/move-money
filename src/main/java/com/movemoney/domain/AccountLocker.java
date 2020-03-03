package com.movemoney.domain;

import com.google.common.annotations.VisibleForTesting;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class AccountLocker {

    int concurrencyLevel = 1 << 8;   // 256 locks
    private final Lock[] locks = Stream.generate(ReentrantLock::new)
            .limit(concurrencyLevel)
            .toArray(Lock[]::new);


    public void lock(Account.Id accountId) {
        Lock lock = locks[compute(accountId)];
        lock.lock();
    }

    public void unlock(Account.Id accountId) {
        Lock lock = locks[compute(accountId)];
        lock.unlock();
    }

    public int compute(Account.Id accountId) {
        return accountId.hashCode() & (concurrencyLevel - 1);
    }

    @VisibleForTesting
    Lock[] locks() {
        return locks;
    }
}