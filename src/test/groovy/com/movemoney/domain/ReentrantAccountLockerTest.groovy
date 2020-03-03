package com.movemoney.domain


import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.stream.Stream

import static com.movemoney.service.Fixtures.*

class ReentrantAccountLockerTest extends Specification {


    def "Lock initial size is"() {
        given:
        ReentrantAccountLocker locker = new ReentrantAccountLocker();

        expect:
        locker.locks().size() == 256
    }

    def "Locks has been created"() {
        given:
        ReentrantAccountLocker locker = new ReentrantAccountLocker();

        expect:
        Stream.of(locker).forEach({ lock -> lock != null})
    }

    def "Locks compute is within 256 and is consistent"() {
        given:
        ReentrantAccountLocker locker = new ReentrantAccountLocker();

        expect:
        locker.compute(borisId) == locker.compute(borisId)
        locker.compute(borisId) < 256
    }

    def "AccountLocker lock is open for the current thread"() {
        given: "given locker is ready"
        ExecutorService executor = Executors.newFixedThreadPool(1);
        ReentrantAccountLocker locker = new ReentrantAccountLocker();

        when: "when borises account is locked"
        locker.lock(borisId)

        then:"attempt fails"
        (locker.locks())[locker.compute(borisId)].tryLock()

    }

    def "AccountLocker locks underlying lock for other threads"() {
        given: "given locker is ready"
        ExecutorService executor = Executors.newFixedThreadPool(1);
        ReentrantAccountLocker locker = new ReentrantAccountLocker();

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
