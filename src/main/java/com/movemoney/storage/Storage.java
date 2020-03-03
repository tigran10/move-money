package com.movemoney.storage;

import com.movemoney.domain.Account;
import com.movemoney.domain.BacsTransaction;

import java.util.List;
import java.util.Optional;

public interface Storage {

    List<Account> findAccounts();

    Optional<Account> findAccount(Account.Id id);

    void createAccount(Account account) throws StorageException;

    void updateAccount(Account.Id id, Account account) throws StorageException;

    void deleteAccount(Account.Id id) throws StorageException;

    List<BacsTransaction> findTransactions(Account.Id id);

    Optional<BacsTransaction> findTransaction(BacsTransaction.Id id);

    void storeTransaction(BacsTransaction bacsTransaction);
}
