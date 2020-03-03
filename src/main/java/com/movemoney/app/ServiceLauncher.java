package com.movemoney.app;

import com.movemoney.app.codecs.AccountDataCodec;
import com.movemoney.app.codecs.TransferInstructionCodec;
import com.movemoney.app.dto.AccountData;
import com.movemoney.app.dto.MoveMoneyInstruction;
import io.vavr.jackson.datatype.VavrModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.DatabindCodec;

public class ServiceLauncher extends AbstractVerticle {


    @Override
    public void start(Future<Void> done) {
        registerJacksonModules();
        registerCodecs(vertx);
    }

    private void registerJacksonModules() {
        DatabindCodec.mapper().registerModule(new VavrModule());
        DatabindCodec.prettyMapper().registerModule(new VavrModule());
    }

    private void registerCodecs(Vertx vertx) {
        vertx.eventBus().registerDefaultCodec(MoveMoneyInstruction.class, new TransferInstructionCodec());
        vertx.eventBus().registerDefaultCodec(AccountData.class, new AccountDataCodec());
    }
}