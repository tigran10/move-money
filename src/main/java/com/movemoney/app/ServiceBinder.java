package com.movemoney.app;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.movemoney.app.config.AppConfig;
import com.movemoney.domain.AccountLocker;
import com.movemoney.domain.ReentrantAccountLocker;
import com.movemoney.service.TransactionManager;
import com.movemoney.storage.MemoryStorage;
import com.movemoney.storage.Storage;
import io.vertx.core.Vertx;


public class ServiceBinder extends AbstractModule {

    @Provides
    @Singleton
    public Storage provideStorage() {
        return new MemoryStorage();
    }

    @Provides
    @Singleton
    public MoveMoneyController provideController(Vertx vertx, Storage storage) {
        return new MoveMoneyController(vertx, storage);
    }

    @Provides
    @Singleton
    public AccountLocker accountLocker() {
        return new ReentrantAccountLocker();
    }

    @Provides
    @Singleton
    public TransactionManager provideTransactionManager(Storage storage, ReentrantAccountLocker accountLocker) {
        return new TransactionManager(storage, accountLocker);
    }

    @Provides
    @Singleton
    public AppConfig provideAppConfig() {
        return new AppConfig(30, 30, 8181);
    }

    @Override
    protected void configure() {

    }
}