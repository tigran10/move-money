package com.movemoney.app;

import io.vertx.core.Handler;

@FunctionalInterface
public interface ThrowingHandler<T, E extends Throwable> {
    void handle(T t) throws E;

    static <T, E extends Throwable> Handler<T> unchecked(ThrowingHandler<T, E> f) {
        return t -> {
            try {
                f.handle(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}