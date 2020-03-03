package com.movemoney.service;

import com.movemoney.domain.Account;
import com.movemoney.domain.BacsTransaction;
import com.movemoney.storage.Storage;
import com.movemoney.storage.StorageException;

import java.util.List;
import java.util.Optional;

public class FastStorage implements Storage {
    @Override
    public List<Account> findAccounts() {
        return null;
    }

    @Override
    public Optional<Account> findAccount(Account.Id id) {
        return Optional.empty();
    }

    @Override
    public void createAccount(Account account) throws StorageException {

    }

    @Override
    public void updateAccount(Account.Id id, Account account) throws StorageException {

    }

    @Override
    public void deleteAccount(Account.Id id) throws StorageException {

    }

    @Override
    public List<BacsTransaction> findTransactions(Account.Id id) {
        return null;
    }

    @Override
    public Optional<BacsTransaction> findTransaction(BacsTransaction.Id id) {
        return Optional.empty();
    }

    @Override
    public void storeTransaction(BacsTransaction bacsTransaction) {

    }
}
