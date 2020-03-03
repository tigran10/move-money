package com.movemoney.service

import com.movemoney.app.dto.MoveMoneyInstruction
import com.movemoney.domain.AccountLocker
import com.movemoney.storage.Storage
import io.vavr.control.Try
import spock.lang.Specification

import static com.movemoney.service.Fixtures.*

class TransactionLockUnlockTest extends Specification {

    Storage storage
    TransactionManager transactionManager
    AccountLocker locker

    def setup() {
        storage = Stub(Storage)
        locker = Mock(AccountLocker)
        transactionManager = Spy(new TransactionManager(storage, locker))
    }

    def "TransactionManager should lock and unlock accounts for successful ops"() {
        given:
        storage.findAccount(borisId) >> Optional.of(boris)
        storage.findAccount(theresaId) >> Optional.of(theresa)
        def instruction = new MoveMoneyInstruction(borisId, theresaId, hundred)

        when:
        transactionManager.moveMoney(instruction)

        then:
        1 * locker.lock(borisId)
        1 * locker.lock(theresaId)
        1 * locker.unlock(borisId)
        1 * locker.unlock(theresaId)
    }

    def "TransactionManager should unlock accounts even when crediting failed"() {
        given:
        storage.findAccount(borisId) >> Optional.of(boris)
        storage.findAccount(theresaId) >> Optional.of(theresa)
        transactionManager.creditBalance(_,_) >> Try.Failure.failure(new RuntimeException("random error"))

        when:
        transactionManager.moveMoney(new MoveMoneyInstruction(borisId, theresaId, hundred))

        then:
        1 * locker.lock(borisId)
        1 * locker.lock(theresaId)
        1 * locker.unlock(borisId)
        1 * locker.unlock(theresaId)
    }

    def "TransactionManager should unlock accounts even when debiting failed"() {
        given:
        storage.findAccount(borisId) >> Optional.of(boris)
        storage.findAccount(theresaId) >> Optional.of(theresa)
        transactionManager.debitBalance(_,_) >> Try.Failure.failure(new RuntimeException("random error"))

        when:
        transactionManager.moveMoney(new MoveMoneyInstruction(borisId, theresaId, hundred))

        then:
        1 * locker.lock(borisId)
        1 * locker.lock(theresaId)
        1 * locker.unlock(borisId)
        1 * locker.unlock(theresaId)
    }

}
