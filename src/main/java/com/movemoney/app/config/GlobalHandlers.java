package com.movemoney.app.config;

import com.movemoney.ex.MoveMoneyExceptionWithCode;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.movemoney.storage.ExeptionErrorCode.POSSIBLE_MESSAGE_REPLY_ERROR;


public class GlobalHandlers {

    private final static Logger LOGGER = Logger.getLogger("GlobalHandlers");

    private GlobalHandlers() {
    }

    public static void lbCheck(RoutingContext ctx) {
        ctx.response().end("ok");
    }

    public static void error(RoutingContext ctx) {
        int status;
        String msg;
        Throwable failure = ctx.failure();

        msg = "Sorry, something went wrong";
        status = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();

        if (MoveMoneyExceptionWithCode.class.isAssignableFrom(failure.getClass())) {
            msg = failure.getMessage();
            status = HttpResponseStatus.BAD_REQUEST.code();
        }
        // this messy as hell, reason being, vertx is stupid, and replies back with custom messages for event base verticle.
        // so its a bit crazy to trace back the original cause
        else if (isReplyExceptionCausedByOurCustomException(failure)) {
            LOGGER.log(Level.SEVERE, failure.getMessage(), failure.getCause());

            msg = failure.getMessage();
            status = HttpResponseStatus.BAD_REQUEST.code();
        } else {
            LOGGER.log(Level.SEVERE, "Unknown Exception, things are bad, call everyone, raise on next retro meeting");
            LOGGER.log(Level.SEVERE, failure.getMessage(), failure);
        }

        // Log the error, and send a json encoded response.
        JsonObject res = new JsonObject().put("status", status).put("message", msg);
        ctx.response().setStatusCode(status).end(res.encode());
    }

    private static boolean isReplyExceptionCausedByOurCustomException(Throwable failure) {
        return ReplyException.class.isAssignableFrom(failure.getClass()) && ((ReplyException) failure).failureCode() == POSSIBLE_MESSAGE_REPLY_ERROR.code;
    }
}