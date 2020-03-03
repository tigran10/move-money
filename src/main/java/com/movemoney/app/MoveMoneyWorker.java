package com.movemoney.app;

import com.google.inject.Inject;
import com.movemoney.app.dto.MoveMoneyInstruction;
import com.movemoney.service.TransactionManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.Json;

import java.util.logging.Logger;

import static com.movemoney.storage.ExeptionErrorCode.POSSIBLE_MESSAGE_REPLY_ERROR;


public class MoveMoneyWorker extends AbstractVerticle {

    private final static Logger LOGGER = Logger.getLogger("MoveMoneyWorker");

    private final TransactionManager service;

    @Inject
    public MoveMoneyWorker(TransactionManager service) {
        this.service = service;
    }

    @Override
    public void start(Future<Void> done) {
        MessageConsumer<MoveMoneyInstruction> consumer = vertx.eventBus().consumer(Events.MOVEMONEY);

        // Handler for the MoveMoney event
        consumer.handler(m -> {
            MoveMoneyInstruction moveMoneyInstruction = m.body();
            LOGGER.info(
                    String.format("New MoveMoney Event from: %s to: %s amount: %s",
                            moveMoneyInstruction.getSourceAccountId(),
                            moveMoneyInstruction.getTargetAccountId(),
                            moveMoneyInstruction.getAmount().displayValue()));

            var retval = service.moveMoney(moveMoneyInstruction);
            retval.onSuccess(moveMoneyResult -> m.reply(Json.encode(moveMoneyResult)))
                    .onFailure(throwable -> m.reply(buildReplyException(throwable, throwable.getMessage())));
        });
        done.complete();
    }

    protected ReplyException buildReplyException(Throwable cause, String message) {
        ReplyException ex = new ReplyException(ReplyFailure.RECIPIENT_FAILURE, POSSIBLE_MESSAGE_REPLY_ERROR.code, message);
        ex.initCause(cause);
        return ex;
    }
}