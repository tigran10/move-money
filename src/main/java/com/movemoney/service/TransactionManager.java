package com.movemoney.service;

import static java.util.function.Function.identity;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.movemoney.app.dto.MoveMoneyResult;
import com.movemoney.app.dto.MoveMoneyInstruction;
import com.movemoney.domain.Account;
import com.movemoney.domain.AccountLocker;
import com.movemoney.domain.Amount;
import com.movemoney.domain.BacsTransaction;
import com.movemoney.storage.ExeptionErrorCode;
import com.movemoney.storage.Storage;
import com.movemoney.storage.TransactionException;
import io.vavr.control.Try;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.stream.Stream;
import java.util.logging.Logger;

import static com.movemoney.storage.ExeptionErrorCode.*;

public class TransactionManager {
    private final static Logger LOGGER = Logger.getLogger("TransactionManager");

    private AccountLocker accountLocker;
    private Storage storage;

    @Inject
    public TransactionManager(Storage storage, AccountLocker accountLocker) {
        this.storage = storage;
        this.accountLocker = accountLocker;
    }

    public Try<MoveMoneyResult> moveMoney(MoveMoneyInstruction instruction) {

        return Try.of(() -> {
            lockAccounts(
                    instruction.getSourceAccountId(),
                    instruction.getTargetAccountId()
            );

            var sourceAccount = findOrFail(instruction.getSourceAccountId());
            var targetAccount = findOrFail(instruction.getTargetAccountId());

            return creditBalance(sourceAccount, instruction.getAmount())
                    .flatMap(aVoid -> debitBalance(targetAccount, instruction.getAmount()))
                    .map(aVoid -> successfulMoveMoneyResult(instruction))
                    .onFailure(throwable -> rollback(sourceAccount, targetAccount))
                    .andThen(() -> transactionSaved(createTransactionLog(instruction)));
        })
                .flatMap(identity())
                .andFinally(() -> unlockAccounts(instruction.getSourceAccountId(), instruction.getTargetAccountId()));
    }


    @VisibleForTesting
    void unlockAccounts(Account.Id... accountsToLock) {
        Stream.of(accountsToLock).forEach(id -> accountLocker.unlock(id));
    }

    @VisibleForTesting
    void lockAccounts(Account.Id... accountsToLock) {
        Stream.of(accountsToLock).forEach(id -> accountLocker.lock(id));
    }

    @VisibleForTesting
    void rollback(Account... lastCorrectState) {
        Try.run(() ->
                Stream.of(lastCorrectState).forEach(account -> storage.updateAccount(account.getId(), account)))
                .onFailure(exc -> {
                    LOGGER.log(Level.SEVERE, "It was impossible to rollback, state should be manually ", exc);
                });
    }

    @VisibleForTesting
    Account findOrFail(Account.Id id) {
        return storage.findAccount(id).orElseThrow(
                () -> new BadUserRequestException("Account Not Found id:" + id.asString(), ExeptionErrorCode.USER_ERROR));
    }

    @VisibleForTesting
    MoveMoneyResult successfulMoveMoneyResult(MoveMoneyInstruction instruction) {
        return MoveMoneyResult.of(String.format("amount %s moved from %s to %s",
                instruction.getAmount().displayValue(),
                instruction.getSourceAccountId().asString(),
                instruction.getTargetAccountId().asString()));
    }

    BacsTransaction createTransactionLog(MoveMoneyInstruction instruction) {
        return BacsTransaction.of(
                BacsTransaction.Id.autoGenerated(),
                instruction.getSourceAccountId(),
                instruction.getTargetAccountId(),
                LocalDateTime.now(),
                instruction.getAmount()
        );
    }


    @VisibleForTesting
    Try<Void> creditBalance(Account account, Amount amount) {

        return Try.run(() -> {
            var newBalance = subtractBalance(account, amount);

            storage.updateAccount(account.getId(), Account.of(
                    account.getId(),
                    account.getFirstName(),
                    Amount.of(newBalance, account.getOngoingBalance().getCurrency())
            ));
        });
    }

    @VisibleForTesting
    Try<Void> debitBalance(Account account, Amount amount) {

        return Try.run(() -> {
            var newBalance = account
                    .getOngoingBalance()
                    .getValue()
                    .add(amount.getValue());

            storage.updateAccount(account.getId(),
                    Account.of(
                            account.getId(),
                            account.getFirstName(),
                            Amount.of(newBalance, account.getOngoingBalance().getCurrency())
                    ));
        });
    }

    @VisibleForTesting
    void transactionSaved(BacsTransaction instruction) {
        storage.storeTransaction(instruction);
    }

    private BigDecimal subtractBalance(Account account, Amount subtractAmount) {

        BigDecimal subtract = account
                .getOngoingBalance()
                .getValue()
                .subtract(subtractAmount.getValue());

        if (subtract.signum() < 0) {
            throw new TransactionException(String.format("Account: %s do not have enough funds", account.getId().asString()), INSUFFICIENT_AMOUNT);
        }

        return subtract;
    }


}