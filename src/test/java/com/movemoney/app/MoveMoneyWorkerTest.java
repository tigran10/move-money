package com.movemoney.app;



import com.movemoney.app.codecs.AppCodecs;
import com.movemoney.app.config.AppConfig;
import com.movemoney.app.dto.AccountData;
import com.movemoney.app.dto.MoveMoneyInstruction;
import com.movemoney.app.dto.MoveMoneyResult;
import com.movemoney.service.BadUserRequestException;
import com.movemoney.service.TransactionManager;
import com.movemoney.storage.Storage;
import io.vavr.control.Try;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.net.ServerSocket;

import static com.movemoney.app.Fixtures.boris;
import static com.movemoney.app.Fixtures.borisSendsMoneyToTheresaInstruction;
import static com.movemoney.storage.ExeptionErrorCode.USER_ERROR;
import static io.vavr.API.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class MoveMoneyWorkerTest {

    Integer port;

    @Mock
    Storage storage;

    @Mock
    TransactionManager transactionManager;

    @BeforeAll
    public void init(Vertx vertx) throws Exception {
        MockitoAnnotations.initMocks(this);
        vertx.deployVerticle(initServerVerticle(vertx), new DeploymentOptions());
        vertx.deployVerticle(new MoveMoneyWorker(transactionManager), new DeploymentOptions().setWorker(true));

        vertx.eventBus().registerDefaultCodec(MoveMoneyInstruction.class, new AppCodecs.TransferInstructionCodec());
        vertx.eventBus().registerDefaultCodec(AccountData.class, new AppCodecs.AccountDataCodec());

        Thread.sleep(2000); //ci starts earlier
    }

    @AfterAll
    public void teardown(Vertx vertx, VertxTestContext testContext) throws Exception {
        vertx.close(testContext.completing());
    }


    @Test
    public void moveMoneyRepliesDetailsOfTransactionIfSuccess(Vertx vertx, VertxTestContext testContext) {
        //given
        doNothing().when(storage).createAccount(boris);
        when(transactionManager.moveMoney(any())).thenReturn(Success(MoveMoneyResult.of("hey, that was success")));

        var accountData = AccountData.of(boris.getFirstName(), boris.getOngoingBalance());
        WebClient webClient = WebClient.create(vertx);

        //when
        webClient.post(port, "localhost", "/api/movemoney")
                .putHeader("content-type", "application/json")
                .as(BodyCodec.string())
                .sendJson(borisSendsMoneyToTheresaInstruction, testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        JSONAssert.assertEquals("{\"status\":\"hey, that was success\"}",
                                resp.body(),
                                JSONCompareMode.LENIENT);
                        assertThat(resp.statusCode()).isEqualTo(201);
                        testContext.completeNow();

                    });
                }));
    }

    @Test
    public void moveMoneyRepliesDetailsOErrorTransactionIfFails(Vertx vertx, VertxTestContext testContext) {
        //given
        doNothing().when(storage).createAccount(boris);
        when(transactionManager.moveMoney(any())).thenReturn(Try.failure(new BadUserRequestException("not enough money", USER_ERROR)));

        var accountData = AccountData.of(boris.getFirstName(), boris.getOngoingBalance());
        WebClient webClient = WebClient.create(vertx);

        //when
        webClient.post(port, "localhost", "/api/movemoney")
                .putHeader("content-type", "application/json")
                .as(BodyCodec.string())
                .sendJson(borisSendsMoneyToTheresaInstruction, testContext.succeeding(resp -> {
                    testContext.verify(() -> {

                        JSONAssert.assertEquals("{\"status\":400,\"message\":\"not enough money\"}",
                                resp.body(),
                                JSONCompareMode.LENIENT);
                        assertThat(resp.statusCode()).isEqualTo(400);
                        testContext.completeNow();

                    });
                }));
    }


    private ServerVerticle initServerVerticle(Vertx vertx) throws IOException {
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        MoveMoneyController moveMoneyController = new MoveMoneyController(vertx, storage);
        AppConfig appConfig = new AppConfig(30, 30, port);
        return new ServerVerticle(moveMoneyController, appConfig);
    }

}