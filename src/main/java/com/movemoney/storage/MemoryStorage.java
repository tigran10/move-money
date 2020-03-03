package com.movemoney.storage;

import com.movemoney.domain.Account;
import com.movemoney.domain.BacsTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//distributed
public final class MemoryStorage implements Storage {

    private final List<BacsTransaction> bacsTransactions = new ArrayList<>(); //immutabilty
    private final List<Account> accounts = new ArrayList<>();


    @Override
    public List<Account> findAccounts() {
        return accounts;
    }

    @Override
    public Optional<Account> findAccount(Account.Id id) {
        return accounts
                .stream()
                .filter(a -> id.equals(a.getId()))
                .findAny();
    }

    @Override
    public void createAccount(Account account) throws StorageException {
        findAccount(account.getId()).ifPresentOrElse(
                (s) -> {
                    throw new StorageException(ExeptionErrorCode.DUPLICATE_ENTRY);
                },
                () -> accounts.add(account)
        );
    }

    @Override
    public void updateAccount(Account.Id id, Account account) throws StorageException {
        findAccount(account.getId()).ifPresentOrElse(
                (s) -> {
                    deleteAccount(id);
                    accounts.add(account);
                },
                () -> {
                    throw new StorageException(ExeptionErrorCode.RESOURCE_NOT_FOUND);
                }
        );
    }

    @Override
    public void deleteAccount(Account.Id id) throws StorageException {
        findAccount(id).ifPresentOrElse(
                accounts::remove,
                () -> {
                    throw new StorageException(ExeptionErrorCode.RESOURCE_NOT_FOUND);
                }
        );
    }

    @Override
    public List<BacsTransaction> findTransactions(Account.Id id) {

        return bacsTransactions
                .stream()
                .filter(transaction -> transaction.getSourceAccountId().equals(id) || transaction.getTargetAccountId().equals(id))
                .collect(Collectors.toList());

    }

    public Optional<BacsTransaction> findTransaction(BacsTransaction.Id id) {

        return bacsTransactions
                .stream()
                .filter(transaction -> transaction.getId().equals(id))
                .findAny();

    }

    @Override
    public void storeTransaction(BacsTransaction bacsTransaction) {
        findTransaction(bacsTransaction.getId()).ifPresentOrElse(
                (s) -> {
                    throw new StorageException(ExeptionErrorCode.DUPLICATE_ENTRY);
                },
                () -> bacsTransactions.add(bacsTransaction)
        );
    }


}
