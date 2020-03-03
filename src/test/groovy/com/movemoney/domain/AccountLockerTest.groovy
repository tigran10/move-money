package com.movemoney.domain


import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.stream.Stream

import static com.movemoney.service.Fixtures.*

class AccountLockerTest extends Specification {


    def "Lock initial size is"() {
        given:
        AccountLocker locker = new AccountLocker();

        expect:
        locker.locks().size() == 256
    }

    def "Locks has been created"() {
        given:
        AccountLocker locker = new AccountLocker();

        expect:
        Stream.of(locker).forEach({ lock -> lock != null})
    }

    def "Locks compute is within 256 and is consistent"() {
        given:
        AccountLocker locker = new AccountLocker();

        expect:
        locker.compute(borisId) == locker.compute(borisId)
        locker.compute(borisId) < 256
    }

    def "AccountLocker lock is open for the current thread"() {
        given: "given locker is ready"
        ExecutorService executor = Executors.newFixedThreadPool(1);
        AccountLocker locker = new AccountLocker();

        when: "when borises account is locked"
        locker.lock(borisId)

        then:"attempt fails"
        (locker.locks())[locker.compute(borisId)].tryLock()

    }

    def "AccountLocker locks underlying lock for other threads"() {
        given: "given locker is ready"
        ExecutorService executor = Executors.newFixedThreadPool(1);
        AccountLocker locker = new AccountLocker();

        when: "when borises account is locked"
        locker.lock(borisId)

        and: "and same lock is tried to be locked from other thread"
        Future<Boolean> future = executor.submit(new Callable<Boolean>() {
            @Override
            Boolean call() throws Exception {
                return (locker.locks())[locker.compute(borisId)].tryLock()
            }
        });
        Boolean attempt = future.get();

        then:"attempt fails"
        !attempt

    }
}
