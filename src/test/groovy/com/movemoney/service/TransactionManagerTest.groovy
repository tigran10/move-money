package com.movemoney.service

import com.movemoney.app.dto.MoveMoneyInstruction
import com.movemoney.domain.Account
import com.movemoney.domain.AccountLocker
import com.movemoney.domain.Amount
import com.movemoney.domain.BacsTransaction
import com.movemoney.domain.Currency
import com.movemoney.storage.Storage
import io.vavr.control.Try
import spock.lang.Specification

import static com.movemoney.service.Fixtures.*

class TransactionManagerTest extends Specification {

    Storage storage
    TransactionManager transactionManager

    def setup() {
        storage = Mock(Storage)
        transactionManager = new TransactionManager(storage, new AccountLocker())
    }

    def "TransactionManager should return not found error if source account is not found"() {
        given:
        storage.findAccount(borisId) >> Optional.empty()
        def instruction = new MoveMoneyInstruction(borisId, theresaId, hundred)

        when:
        def result = transactionManager.moveMoney(instruction)

        then:
        result.isFailure()
        result.cause.message == "Account Not Found id:${boris.id.asString()}"
    }

    def "TransactionManager should return not found error if target account is not found"() {
        given:
        storage.findAccount(borisId) >> Optional.of(boris)
        storage.findAccount(theresaId) >> Optional.empty()
        def instruction = new MoveMoneyInstruction(borisId, theresaId, hundred)

        when:
        def result = transactionManager.moveMoney(instruction)

        then:
        result.isFailure()
        result.cause.message == "Account Not Found id:${theresa.id.asString()}"
    }

    def "TransactionManager should return error if no sufficient fund available on source account "() {

        given:
        def borisWithTenPounds = Account.of(borisId, "Bob", ten);
        storage.findAccount(borisId) >> Optional.of(borisWithTenPounds)
        storage.findAccount(theresaId) >> Optional.of(theresa)


        when:
        def result = transactionManager.moveMoney(new MoveMoneyInstruction(borisId, theresaId, hundred))

        then:
        result.isFailure()
        result.cause.message == "Account: ${borisId.asString()} do not have enough funds"
    }


    def "TransactionManager should return error if amount cant be credited for unknown reason"() {

        given: "Transaction will fail with random error, during crediting source account"
        def failingTransactionManager = Spy(new TransactionManager(storage, new AccountLocker()))
        failingTransactionManager.creditBalance(_, _) >> Try.Failure.failure(new RuntimeException("Random error"))
        storage.findAccount(borisId) >> Optional.of(boris)
        storage.findAccount(theresaId) >> Optional.of(theresa)

        when: "instruction is submitted"
        def result = failingTransactionManager.moveMoney(new MoveMoneyInstruction(borisId, theresaId, hundred))

        then: "error is bubbled back"
        result.isFailure()
        result.cause.message == "Random error"

        and: "rollback is called"
        1 * failingTransactionManager.rollback(boris, theresa)

        and: "debiting other account skipped"
        0 * failingTransactionManager.debitBalance(_, _)
    }


    def "TransactionManager should return error if amount cant be debited, and rollback done if it was credited from source account"() {

        given: "Transaction will fail with random error, during debiting target account"
        def failingTransactionManager = Spy(new TransactionManager(storage, new AccountLocker()))
        failingTransactionManager.debitBalance(_, _) >> Try.Failure.failure(new RuntimeException("Random error"))
        storage.findAccount(borisId) >> Optional.of(boris)
        storage.findAccount(theresaId) >> Optional.of(theresa)

        when: "instruction is submitted"
        def result = failingTransactionManager.moveMoney(new MoveMoneyInstruction(borisId, theresaId, hundred))

        then: "error is bubbled back"
        result.isFailure()
        result.cause.message == "Random error"

        and: "rollback is called"
        1 * failingTransactionManager.rollback(boris, theresa)

        and: "transaction has not been saved"
        0 * failingTransactionManager.transactionSaved(_)
    }


    def "TransactionManager should credit source account, and debit target account"() {

        given: "boris and theresa exist sufficient funds"
        def localTransactionManager = Spy(new TransactionManager(storage, new AccountLocker()))
        storage.findAccount(borisId) >> Optional.of(boris)
        storage.findAccount(theresaId) >> Optional.of(theresa)


        when: "boris sends ten pounds to theresa"
        def result = localTransactionManager.moveMoney(new MoveMoneyInstruction(borisId, theresaId, ten))

        then: "then result is green"
        result.isSuccess()


        and: "accounts been credited and debited once"
        1 * localTransactionManager.creditBalance(boris, ten)
        1 * localTransactionManager.debitBalance(theresa, ten)

        and: "and ten pounds has been credited to boris"
        1 * storage.updateAccount(borisId,
                Account.of(borisId,
                        boris.firstName,
                        Amount.of(boris.ongoingBalance.value.subtract(ten.getValue()), Currency.GBP)))

        and: "and ten pounds has been debited to theresa"
        1 * storage.updateAccount(theresaId,
                Account.of(theresaId,
                        theresa.firstName,
                        Amount.of(theresa.ongoingBalance.value.add(ten.getValue()), Currency.GBP)))

        and: "transaction has been saved"
        1 * localTransactionManager.transactionSaved(_)

        and: "rollback never called"
        0 * localTransactionManager.rollback(boris, theresa)
    }

    def "TransactionManager should save transaction for successful ops"() {

        given: "boris and theresa exist sufficient funds"
        def localTransactionManager = Spy(new TransactionManager(storage, new AccountLocker()))
        storage.findAccount(borisId) >> Optional.of(boris)
        storage.findAccount(theresaId) >> Optional.of(theresa)


        when: "boris sends ten pounds to theresa"
        def result = localTransactionManager.moveMoney(new MoveMoneyInstruction(borisId, theresaId, ten))

        then: "then result is green"
        result.isSuccess()

        and: "transaction has been saved"
        1 * localTransactionManager.transactionSaved(_) >> { arguments ->
            final BacsTransaction transaction = arguments.get(0)
            assert transaction.amount.equals(ten)
            assert transaction.sourceAccountId.equals(borisId)
            assert transaction.targetAccountId.equals(theresaId)
        }

        and: "rollback never called"
        0 * localTransactionManager.rollback(boris, theresa)
    }

}